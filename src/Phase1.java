package de.spieleck.ingress.hackstat;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Locale;

import org.apache.log4j.Logger;

import static de.spieleck.ingress.hackstat.GenericHelper.*;
import static de.spieleck.ingress.hackstat.HackFilter.*;

public class Phase1
    implements Globals
{
  private final static Logger L = Logger.getLogger(Phase1.class);

  private final static int WEEK = 60*60*24*7;

  public final static String WEEKS = "Weeks";

  private static DateFilter[] times;
  private final static Map<String,String> ABBR = new HashMap<>();
  static
  {
      try {
          BufferedReader br = new BufferedReader(new FileReader("dates.dat"));
          ArrayList<String> changeDates = new ArrayList<>();
          String line;
          while ( ( line = br.readLine() ) != null ) {
              line = line.trim();
              if ( line.charAt(0) != '#' ) changeDates.add(line);
          }
          br.close();
          Collections.sort(changeDates);
          int noOfDates = changeDates.size();
          times = new DateFilter[noOfDates+1];
          int idx = noOfDates;
          times[idx--] = new BeforeThanFilter(changeDates.get(0));
          for(int i = 1; i < noOfDates; i++) {
              times[idx--] = new BetweenDateFilter(changeDates.get(i-1), changeDates.get(i));
          }
          assert(idx == 0);
          times[idx--] = new LaterThanFilter(changeDates.get(noOfDates-1));
      } catch  ( Exception ex ) {
          L.warn("Cannot construct time filters", ex);
      }
      ABBR.put(ADA, "ADARef");
      ABBR.put(JARVIS, "JARVIS");
      ABBR.put(LINK_AMP, "LinkAmp");
      ABBR.put(RESO, "Reso");
      ABBR.put(FORCE_AMP, "ForceAmp");
      ABBR.put(US, "UltraStr");
  }

  // No output without subsummarizers.
  private final static Summarizer NO_SUMMARY = new CombinedSummarizer();

	private List<HackResult> allHacks = new ArrayList<HackResult>();

  private final DataParser parser = new DataParser();

  private final static int SHORT = 0;
  private final static int ADAPTIVE = 1;
  private final static int LONG = 2;

  private int longMode;

  private double startTime = Double.MAX_VALUE;
  private double endTime = 0;

	public Phase1(int longMode)
	{
      this.longMode = longMode;
	}

  public void add(String s)
		throws IOException
	{
      try {
          URL u = new URL(s);
          HttpURLConnection huc = (HttpURLConnection) u.openConnection();
          huc.connect();
          List<HackResult> d = parser.grok(huc.getInputStream());
          add(d, s);
      }
      catch(Exception ex) {
          L.warn("URL processing.", ex);
          add(new File(s));
      }
  }

	public void add(File fi)
		throws IOException
	{
		FileReader fr = new FileReader(fi);
		List<HackResult> d = parser.grok(fi);
    add(d, fi.toString());
  };

  public void add(List<HackResult> d, String inputID)
  {
    for(HackResult hackResult : d) {
        startTime = Math.min(startTime, hackResult.timestamp);
        endTime = Math.max(endTime, hackResult.timestamp);
        int hackLevel = hackResult.getLevel();
        boolean isClear = true;
        for(HackItem hackItem : hackResult.hack.items) {
            if ( MEDIA.equals(hackItem.object) && hackItem.level > 0 ) {
                hackItem.level = 0;
                L.debug("Media fixed@"+hackResult);
            }
            if ( CUBE.equals(hackItem.object) && hackItem.level != hackLevel ) {
                plausi("WrongCube", inputID, hackItem, hackResult);
                isClear = false;
            } else if ( hackItem.level  > 0 && hackItem.level > hackLevel+2 ) {
                plausi("ItemTooHigh", inputID, hackItem, hackResult);
                isClear = false;
            } else if ( hackItem.level  > 0 && hackItem.level < hackLevel-1 ) {
                plausi("ItemTooLow", inputID, hackItem, hackResult);
                isClear = false;
            }
        }
        if ( isClear ) {
            allHacks.add(hackResult);
        }
    }
    L.info(String.format("***** #allHacks=%d of %d, endTime=%tc",allHacks.size(),d.size(),1000*(long)endTime));
	}

  private static void plausi(String mark, String inputID, HackItem item, HackResult hack)
  {
      L.warn(String.format("%-12s@%13s:%-22s %-4s %s",mark, inputID, hack._id,item,hack));
  }

  /**
   * Base way to attack KNIME or something common to the data.
   * Please add columns as required!
   */
  public void dumpCSV(String fName, String sep)
      throws IOException
  {
      File fi = new File(fName);
      PrintWriter pw = new PrintWriter(new FileWriter(fi));
      for(HackResult hackResult : allHacks) {
          pw.print(hackResult.timestamp);
          pw.print(sep);
          pw.print(hackResult.getPortalLevel());
          pw.print(sep);
          pw.print(hackResult.getPlayerLevel());
          pw.print(sep);
          pw.print(hackResult.getItemCount());
          pw.print(sep);
          pw.print(FRIEND.equals(hackResult.hack.type)? 1 : 0);
          pw.println();
      }
      pw.close();
      L.info("Finished <"+fName+">.");
  }

	public FullResult stats(Summarizer out, HackFilter... filters)
      throws Exception
	{
      return stats(out, null, filters);
  }

	public FullResult stats(Summarizer out, FullResult reference, HackFilter... filters)
      throws Exception
	{
		Map<String,Integer> types = new HashMap<>();
		Map<String,Integer> basics = new HashMap<>();
		Map<Integer,Integer> nkeys = new HashMap<>();
		Map<Integer,Integer> levels = new HashMap<>();
		Map<Integer,Integer> levelTotals = new HashMap<>();
		Map<Integer,Integer> counts = new HashMap<>();
		Map<Integer,Integer> noOfItems = new HashMap<>();
		Map<Integer,Integer> noOfResos = new HashMap<>();
		Map<Integer,Integer> noOfXmps = new HashMap<>();
		Map<Integer,Integer> noOfOther = new HashMap<>();
		Map<String,Integer> noOfPattern = new HashMap<>();
		Map<String,Integer> noOfUSPattern = new HashMap<>();
		Map<String,Integer> noResoPattern = new HashMap<>();
		Map<String,Integer> noXMPPattern = new HashMap<>();
		Map<String,Integer> noOfPatternBig = new HashMap<>();
		Map<String,Integer> noOfPatternHuge = new HashMap<>();
		Map<String,Integer> levelPattern = new HashMap<>();
		Map<String,Integer> rareItems = new HashMap<>();
		Map<String,Integer> hackers = new HashMap<>();
		Map<String,Integer> weeks = new HashMap<>();
		IMatrix<Integer,Integer> levelResults = new IMatrix<>();
		Map<Integer,Stats1D> levelResults26 = new HashMap<>();
		Map<Integer,Integer> levelCounts = new HashMap<>();
		Map<String,Stats1D> crossItems = new HashMap<>();
		Map<Integer,Stats1D> playerLevelVsKeys = new HashMap<>();
		Map<Integer,Stats1D> hackLevelVsKeys = new HashMap<>();
		Map<String,Integer> getKeysStatsHas = new HashMap<>();
		Map<String,Integer> getKeysStatsHasnot = new HashMap<>();
		Stats2D overHacks = new Stats2D();
		Stats2D overHacksNPC = new Stats2D();
		int totalCount = 0;
		int totalCountHas = 0;
		int totalCountHasnot = 0;
    // XXX Stupid duplication of code to determine all possible item types to count zeros!!
    Set<String> allFullItems = new HashSet<>();
outerloop1:    
		for(HackResult hackResult : allHacks) {
		  int hackLevel = hackResult.getLevel();
		  for(HackFilter fi : filters) {
			  if ( !fi.accept(hackResult) ) continue outerloop1;
		  }
		  for(HackItem hackItem : hackResult.hack.items) {
        String fullItem = shortItemName(hackItem);
        if ( hackItem.level > 0 ) {
          int relLevel = hackItem.level - hackLevel;
          fullItem += "."+relLevel;
			  }
        allFullItems.add(fullItem);
      }
    }
outerloop:		
		for(HackResult hackResult : allHacks) {
		  int hackLevel = hackResult.getLevel();
      HashSet<String> notSeenItems = new HashSet<>(allFullItems);
		  for(HackFilter fi : filters) {
			  if ( !fi.accept(hackResult) ) continue outerloop;
		  }
      increment(basics, "Hacks", 1);
		  totalCount++;
      boolean hasKey = hackResult.hack.nkeys > 0;
      if ( hasKey ) totalCountHas++; else totalCountHasnot++;
		  int sumCount = 0;
		  int sumResoCount = 0;
		  int sumXmpCount = 0;
		  int sumUSCount = 0;
		  int sumOtherCount = 0;
		  int sumKeyCount = 0;
		  int sumShieldCount = 0;
		  int sumCubeCount = 0;
		  String levelBase = Integer.toString(hackResult.getOverLevel());
		  int relLevelCount = 0;
		  int relLevelSum = 0;
		  int relLevelCountNPC = 0;
		  int relLevelSumNPC = 0;
      int[] hackLevelSum = new int[9];
      int[] resoPattern = new int[4];
      int[] xmpPattern = new int[4];
      increment(nkeys, hackResult.hack.nkeys, 1);
      increment(hackers, hackResult.hacker.name, 1);
      long week = ((long) (hackResult.timestamp/ WEEK))*WEEK * 1000;
      increment(weeks, String.format("%ty-%<tm-%<td", week), 1);
      increment(levelTotals, hackLevel, 1);
      boolean hackContainsKey = false;
		  for(HackItem hackItem : hackResult.hack.items) {
        int count = hackItem.quantity;
        sumCount += count;
        increment(basics, "Items", 1);
        increment(counts, count, 1);
        String shortName = shortItemName(hackItem);
        increment(types, shortName, count);
        switch ( hackItem.object ) {
          case RESO: sumResoCount += hackItem.quantity; break;
          case XMP: sumXmpCount += hackItem.quantity; break;
          case US: sumUSCount += hackItem.quantity; break;
          case KEY: sumKeyCount += hackItem.quantity; hackContainsKey = true; break;
          case SHIELD: sumShieldCount += hackItem.quantity; break;
          case CUBE: sumCubeCount += hackItem.quantity; break;
        }
        String fullItem = shortName;
        increment(levelCounts, hackLevel, 1);
        hackLevelSum[hackItem.level] += count;
        if ( hackItem.hasRarity() ) {
            increment(rareItems, hackItem.toString(), count);
        }
        if ( hackItem.level > 0 ) {
          levelResults.inc(hackLevel, hackItem.level, count);
          // XXX this somehow assumes L8 player!
          int relLevel = hackItem.level - hackLevel;
          if ( RESO.equals(hackItem.object) ) resoPattern[relLevel+1] = count;
          if ( XMP.equals(hackItem.object) ) xmpPattern[relLevel+1] = count;
          fullItem += "."+relLevel;
          relLevelCount++;
          relLevelSum += relLevel;
          if ( !CUBE.equals(hackItem.object) ) {
            relLevelCountNPC++;
            relLevelSumNPC += relLevel;
          }
          increment(levels, relLevel, count);
          increment(levelPattern, levelBase+relLevel, count);
        }
        else {
          sumOtherCount += count;
        }
        if ( hackLevel > 1 && hackLevel < 7 ) {
            for(int i = -1; i < 3; i++) {
                increment(levelResults26, i, hackLevelSum[i+hackLevel]);
            }
        }
        notSeenItems.remove(fullItem);
        increment(crossItems, fullItem, count);
      }
      for(String noItem : notSeenItems) increment(crossItems, noItem, 0);
      increment(noOfResos, sumResoCount, 1);
      increment(noOfXmps, sumXmpCount, 1);
      increment(noOfOther, sumOtherCount, 1);
      increment(noOfUSPattern, Integer.toString(sumResoCount) + sumXmpCount + sumUSCount, 1);
      increment(noResoPattern, ia2str(resoPattern), 1);
      increment(noXMPPattern, ia2str(xmpPattern), 1);
      increment(noOfPattern, Integer.toString(sumResoCount) + sumXmpCount, 1);
      increment(noOfPatternBig, Integer.toString(sumResoCount) + sumXmpCount + sumOtherCount, 1);
		  increment(noOfPatternHuge, Integer.toString(sumResoCount) + sumXmpCount + "-" + sumKeyCount + sumShieldCount, 1);
		  increment(noOfItems, sumCount, 1);
      increment(playerLevelVsKeys, hackResult.getPlayerLevel(), sumKeyCount);
      increment(hackLevelVsKeys, hackLevel, sumKeyCount);
      increment(hasKey ? getKeysStatsHas : getKeysStatsHasnot, hackContainsKey ?"gets":"----", 1);
		  int overLevel = hackResult.getOverLevel();
		  if ( relLevelCount > 0 ) {
			  overHacks.add(overLevel,1.0*relLevelSum/relLevelCount);
		  }
		  if ( relLevelCountNPC > 0 ) {
			  overHacksNPC.add(overLevel,1.0*relLevelSumNPC/relLevelCountNPC);
		  }
		}
    if ( totalCount == 0 ) return null;
    // if (longMode != LONG || totalCount < 10 ) return null;
    FullResult res = new FullResult(filters, out);
    out.startColumn(Util.append(new StringBuilder(), filters));
		res.summary("Basics", basics, totalCount, true, reference);
		res.summary("With Key", getKeysStatsHas, totalCountHas, true, reference);
		res.summary("WO Key", getKeysStatsHasnot, totalCountHasnot, true, reference);
		if(longMode == LONG) res.summary("hack levels", levelTotals, totalCount, true, reference);
		res.summary("Items", noOfItems, totalCount, true, reference);
		res.summary("Resos", noOfResos, totalCount, true, reference);
		res.summary("ResoPatterns", noResoPattern, totalCount, true, reference);
		res.summary("Xmps", noOfXmps, totalCount, true, reference);
		res.summary("XMPPatterns", noXMPPattern, totalCount, true, reference);
		res.summary("Other", noOfOther, totalCount, true, reference);
		if(longMode == LONG) res.summary("nkeys", nkeys, totalCount, true, reference);
		res.summary("Short Patterns", noOfPattern, totalCount, true, reference);
		if(longMode == LONG) res.summary("US Patterns", noOfUSPattern, totalCount, true, reference);
		res.summary("Rare Items", rareItems, totalCount, true, reference);
		if(longMode == LONG) res.summary("Long Patterns", noOfPatternBig, totalCount, true, reference);
		if(longMode == LONG) res.summary("Huge Patterns", noOfPatternHuge, totalCount, true, reference);
		res.summary("Items by Type", types, totalCount, true, reference);
		res.summary("Items by Level", levels, totalCount, true, reference);
		if(longMode == LONG) res.summary("Patterns of Items by Overlevel, Level", levelPattern, totalCount, true, reference);
		res.summary2("Items x Level", crossItems, totalCount, true, reference);
		if(longMode == LONG) res.summary2("Player Level vs Keys", playerLevelVsKeys, totalCount, true, reference);
		if(longMode == LONG) res.summary2("Hack Level vs Keys", hackLevelVsKeys, totalCount, true, reference);
    if(longMode == LONG ) {
        for(int i = 1; i <= 8; i++) {
            if ( longMode == LONG || i == 1 || i == 7 || i == 8 ) res.summary("Hack Level L"+i, levelResults.getRow(i), levelCounts.get(i), false, reference);
            if ( longMode != LONG && i == 2 ) {
                res.summary2("Hack Level L2-6 rel.", levelResults26, totalCount, true, reference);
            }
        }
		}
		if(longMode == LONG) res.summary("Hackers", hackers, totalCount, true, reference);
		if(longMode == LONG) res.summary(WEEKS, weeks, totalCount, true, reference);
		out.value("overHacking-Correlation", overHacks.correlation());
		out.value("overHacking-NonPC-Correlation", overHacksNPC.correlation());
    out.endColumn();
    return res;
  }

  public static String ia2str(int[] x)
  {
      StringBuilder res = new StringBuilder(x.length);
      int sum = 0;
      for(int i : x) sum += i;
      res.append(sum).append(": ");
      for(int i : x) res.append(i);
      return res.toString();
  }

  public String shortItemName(HackItem item)
  {
      String name = item.object;
      String name2 = ABBR.get(name);
      return name2 == null ? name : name2;
  }

  /**
   * Class to collect all data, to allow something like a G-Test for "relevancy".
   * The details might probably be programmed out in a "next" version of the programm,
   * since we might still consider it nice, to have all the data analyzed.
   */
  public class FullResult
  {
      private HackFilter[] filters;
      private Summarizer out;
      private HashMap<String,DiscreteDistr> allData = new LinkedHashMap<>();

      private FullResult(HackFilter[] filters, Summarizer out)
      {
          this.filters = filters;
          this.out = out;
      }

      public HackFilter[] getFilters() { return filters; }

      public Set<String> keys() { return allData.keySet(); }

      public DiscreteDistr get(String key) { return allData.get(key); }

      private void summary(String label, Map<? extends Object, Integer> data, Integer norm, boolean average, FullResult reference)
          throws Exception
      {
        if ( data == null ) return;
        int sum = 0;
        boolean allInt = true;
        Object[] keys = new Object[data.size()];
        int i = 0;
        for(Object key : data.keySet()) {
          keys[i++] = key;
          sum += data.get(key);
          allInt &= (key instanceof Integer);
        }
        Arrays.sort(keys);
        if ( norm == null ) {
            norm = Integer.valueOf(sum);
        }
        double hsum = 0.0;
        double f = 100.0 / norm;
        double f2 = 100.0 / sum;
        if ( allInt ) {
            for(Object key : keys) {
                hsum += data.get(key) * ((Integer)key);
            }
        }
        out.start(label);
        String description = String.format("total=%d, norm=%d ", sum, norm);
        // if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
        out.description(description);
        if ( allInt && average ) out.value("_average", hsum/norm);
        out.setNorms(f, f2);
        DiscreteDistr distr = prepareDistr(label);
        for(Object key : keys) {
            out.item(key, data.get(key));
            distr.inc(key, (int) data.get(key));
        }
        addTest2Out(distr, label, reference);
        out.finish(sum);
      }

      private void summary2(String label, Map<? extends Object, Stats1D> data, Integer norm, boolean average, FullResult reference)
          throws Exception
      {
        if ( data == null ) return;
        boolean allInt = true;
        Object[] keys = new Object[data.size()];
        int i = 0, sum = 0;
        for(Object key : data.keySet()) {
          keys[i++] = key;
          sum += data.get(key).sum();
          allInt &= (key instanceof Integer);
        }
        Arrays.sort(keys);
        if ( norm == null ) {
            norm = Integer.valueOf(sum);
        }
        double hsum = 0.0;
        if ( allInt ) {
            for(Object key : keys) {
                hsum += data.get(key).sum() * ((Integer)key);
            }
        }
        out.start(label);
        String description = String.format("total=%d, norm=%d ", sum, norm);
        // if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
        out.description(description);
        if ( allInt && average ) out.value("_average", hsum/norm);
        DiscreteDistr distr = prepareDistr(label);
        for(Object key : keys) {
            out.item(key, data.get(key));
            distr.inc(key, (int) data.get(key).sum());
        }
        addTest2Out(distr, label, reference);
        out.finish(sum);
      }

      private void addTest2Out(DiscreteDistr d1, String label, FullResult reference)
          throws Exception
      {
          if ( reference == null ) return;
          DiscreteDistr d2 = reference.get(label);
          Set freedom = d1.combinedKeys(d2);
          if ( freedom.size() == 1 ) return;
          double gtest = d1.gtest(d2);
          if ( gtest == Double.POSITIVE_INFINITY ) return;
          double pochisq = SFunc.pochisq(gtest, freedom.size()-1);
          double changePerc = 100.0 * (1.0 - pochisq);
          out.value("_changePerc", String.format(Locale.US, "%.1f", changePerc));
      }

      private DiscreteDistr prepareDistr(String label)
      {
          DiscreteDistr res = new DiscreteDistr();
          allData.put(label, res);
          return res;
      }

      public String toString() {
          StringBuilder sb = new StringBuilder();
          sb.append("F[");
          Util.append(sb, filters);
          sb.append("]");
          return sb.toString();
      }
  }

  private static void compareDist(FullResult base, FullResult res)
  {
      System.err.println(Util.append(new StringBuilder(), res.getFilters()));
      double sum = 0.0;
      double max = 0.0;
      for(String key : base.keys()) {
          DiscreteDistr d1 = base.get(key);
          DiscreteDistr d2 = res.get(key);
          Set freedom = d1.combinedKeys(d2);
          if ( d2 == null ) continue;
          double gtest = d1.gtest(d2);
          double chiBound = SFunc.critchi(0.95, freedom.size()-1);
          double normed = gtest / chiBound;
          System.err.println(String.format("   gtest(%-23s)=%8.2e  (%3d;%7.2e) %10.0f%%",
                  key, gtest, freedom.size()-1, chiBound, normed));
          if ( gtest > 0 ) {
              sum += normed;
              max = Math.max(max, normed);
          }
      }
      System.err.println(String.format("   === %8.2e/%8.2e", sum, max));
      System.err.println();
  }

  private static void selectiveAnalysis(String key, List<FullResult> resultList)
  {
      FullResult[] results = resultList.toArray(new FullResult[resultList.size()]);
      double[] score = new double[results.length];
      double sum = 0.0;
      int count = 0;
      for(int i = 0; i < results.length; i++) {
          FullResult res1 = results[i];
          if ( res1 == null ) continue;
          DiscreteDistr d1 = res1.get(key);
          if ( d1 == null ) continue;
          for(int j = i+1; j < results.length; j++) {
              FullResult res2 = results[j];
              if ( res2 == null ) continue;
              if ( Math.abs(res1.getFilters().length - res2.getFilters().length) != 1 ) continue;
              DiscreteDistr d2 = res2.get(key);
              if ( d2 == null ) continue;
              Set freedom = d1.combinedKeys(d2);
              double gtest = DiscreteDistr.gtest(d1, d2);
              if ( gtest <= 0.0 ) continue;
              double chiBound = SFunc.critchi(0.95, freedom.size()-1);
              if ( Double.isNaN(chiBound) ) continue;
              double normed = gtest / chiBound;
              if ( normed <= 1.0 ) continue;
              double deltaScore = (normed-1.0)*normed;
              score[i] += deltaScore/2.0;
              score[j] += deltaScore/2.0;
              sum += deltaScore;
              count++;
          }
      }
      double average = sum/count;
      for(int i = 0; i < score.length; i++) if ( score[i] > 3.0 * average ) {
          System.out.println(String.format("%s %2d %7.2g %s", key, i, score[i]/average, results[i]));
      }
  }

  public List<FullResult> runFilterStack(int longMode, Summarizer o)
      throws Exception
  {
    o.value("Last Hack", String.format("%tF %<tT", 1000*(long) endTime));
    o.value("Total Data", allHacks.size());
    List<FullResult> res = new ArrayList<FullResult>();
    if ( longMode == LONG ) {
        FullResult base1 = stats(o, NO_FILTER);
        res.add(base1);
        for(HackFilter f0 : FRIEND_OR_FOE) {
            FullResult res2 = stats(o, f0);
            res.add(res2);
        }
    }
    FullResult res10 = stats(Summarizer.NO_SUMMARIZER, times[0], FRIEND_FILTER);
    FullResult res11 = stats(Summarizer.NO_SUMMARIZER, times[0], FOE_FILTER);
    LaterThanFilter timeFilter0 = createPercTimeFilter(0.33, times[0], FRIEND_FILTER);
    LaterThanFilter timeFilter1 = createPercTimeFilter(0.33, times[0], FOE_FILTER);
    FullResult res00 = stats(o, res10, timeFilter0, FRIEND_FILTER);
    FullResult res01 = stats(o, res11, timeFilter1, FOE_FILTER);
    res.add(timeFilter0.compareTo(timeFilter1) < 0 ? res01 : res00);
    res.add(timeFilter0.compareTo(timeFilter1) < 0 ? res00 : res01);
    for(int time = 0; time < times.length; time++) {
        DateFilter timeFilter = times[time];
        if ( longMode == LONG ) {
            FullResult res1 = stats(o, timeFilter);
            res.add(res1);
        }
        for(HackFilter f0 : FRIEND_OR_FOE) {
            FullResult res2 = stats(o, timeFilter, f0);
            res.add(res2);
            if ( time == 0 && f0 == FRIEND_FILTER ) {
                // handled differently now res.add(stats(o, timeFilter, f0, HASKEY_FILTER));
                // res.add(stats(o, timeFilter, f0, CAN_GET_ULTRA));
            }
            else if ( time == 0 && f0 == FOE_FILTER ) {
                // handled differently now res.add(stats(o, timeFilter, f0, HASKEY_FILTER));
            }
            //
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, R8_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, R8_FILTER, NON_P8_FILTER ));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL1_FILTER));
            }
            if(longMode != LONG && time == 0 ) {
                res.add(stats(o, timeFilter, f0, L26_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL2_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL3_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL4_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL5_FILTER));
            }
            if(longMode == LONG) {
                res.add(stats(o, timeFilter, f0, HL6_FILTER));
            }
            if(longMode == LONG || time == 0 ) {
                res.add(stats(o, timeFilter, f0, HL7_FILTER));
            }
            if(longMode == LONG || time == 0 ) {
                res.add(stats(o, timeFilter, f0, HL8_FILTER));
            }
        }
    }
    o.close();
    return res;
	}

  public static boolean similarFilter(FullResult res1, FullResult res2)
  {
      if ( res1 == null || res2 == null || res1.filters == null || res2.filters == null ) return false;
      if ( res1.filters.length != res2.filters.length ) return false;
      if ( !(res1.filters[0] instanceof DateFilter || res1.filters[0] instanceof BetweenDateFilter) ) return false;
      if ( !(res2.filters[0] instanceof DateFilter || res2.filters[0] instanceof BetweenDateFilter) ) return false;
      for(int i = 1; i < res1.filters.length; i++) {
          if ( res1.filters[i] != res2.filters[i] ) return false;
      }
      return true;
  }

  public LaterThanFilter createPercTimeFilter(double perc, HackFilter... fis)
  {
      ArrayList<Long> timeStamps = new ArrayList<Long>();
outerloop:
      for(HackResult res : allHacks) {
          for(HackFilter fi : fis) { 
              if ( !fi.accept(res) ) continue outerloop;
          }
          timeStamps.add((long)res.timestamp);
      }
      Collections.sort(timeStamps);
      int idx = (int) Math.round((timeStamps.size()-1) * (1.0 - perc));
      return new LaterThanFilter(timeStamps.get(idx));
  }

	public static void main(String[] args)
		throws Exception
	{
    int longMode = SHORT;
		Phase1 p1 = new Phase1(longMode);
    CombinedSummarizer o = new CombinedSummarizer();
    o.addSummarizer(new XSLTSummarizer());
    o.addSummarizer(new XSLTSummarizer("layout1.xsl", "out.html"));
		for(String arg : args) p1.add(arg);
    p1.dumpCSV("out.csv",";");
    List<FullResult> res = p1.runFilterStack(SHORT, o);
    /*
    List<FullResult> allFRes = p1.runFilterStack(LONG, NO_SUMMARY);
    FullResult f0 = allFRes.get(0);
    for(String key : f0.keys()) {
        selectiveAnalysis(key, allFRes);
    }
    */
  }

}
