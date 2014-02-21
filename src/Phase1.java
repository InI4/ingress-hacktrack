package de.spieleck.ingress.hackstat;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Locale;

import org.apache.log4j.Logger;

import static de.spieleck.ingress.hackstat.GenericHelper.*;
import static de.spieleck.ingress.hackstat.HackFilter.*;

public class Phase1
    implements Globals, StatCreator
{
  public final static boolean INCL_KEY_AND_MEDIA = false;

  private final static HashSet<String> NOOTHER = new HashSet<>();
  static {
      NOOTHER.add(KEY);
      NOOTHER.add(MEDIA);
      NOOTHER.add(XMP);
      NOOTHER.add(RESO);
  }

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
   * Convert a hack in a small pattern.
   */
  private static String smallPattern(HackResult hackResult) {
      int sumResoCount = 0;
      int sumXmpCount = 0;
      for(HackItem hackItem : hackResult.hack.items) {
          switch ( hackItem.object ) {
              case RESO: sumResoCount += hackItem.quantity; break;
              case XMP: sumXmpCount += hackItem.quantity; break;
          }
      }
      return String.format("%d/%d", sumResoCount, sumXmpCount);
  }

  /**
   * Convert a hack in a list of rare items
   */
  private static List<String> rareItems(HackResult hackResult) {
      List<String> res = new ArrayList<>(); 
      for(HackItem hackItem : hackResult.hack.items) {
          if ( hackItem.hasRarity() ) res.add(hackItem.toString());
      }
      return res;
  }

  /**
   * Base way to attack KNIME or something common to the data.
   * Please add columns as required!
   */
  public void dumpCSV(String fName, String sep, HackFilter... filters)
      throws IOException
  {
      LinkedHashSet<String> patterns = new LinkedHashSet<>();
      LinkedHashSet<String> rares = new LinkedHashSet<>();
outerloop1:
      for(HackResult hackResult : allHacks) {
          for(HackFilter fi : filters) if ( !fi.accept(hackResult) ) continue outerloop1;
          String pat = smallPattern(hackResult);
          patterns.add(pat);
          List<String> items = rareItems(hackResult);
          rares.addAll(items);
      }
      PrintWriter pw = new PrintWriter(new FileWriter(new File(fName)));
      pw.print("# ");
      for(HackFilter f : filters) pw.print(" "+f.toString());
      pw.println();
      pw.print("# ");
      for(String s : patterns) pw.print(" "+s);
      pw.print("  ");
      for(String s : rares) pw.print(" "+s);
      pw.println();
outerloop2:
      for(HackResult hackResult : allHacks) {
          for(HackFilter fi : filters) if ( !fi.accept(hackResult) ) continue outerloop2;
          String pat = smallPattern(hackResult);
          List<String> items = rareItems(hackResult);
          pw.print(String.format("%10.0f", hackResult.timestamp));
          for(String s : patterns) {
              pw.print(sep);
              pw.print(pat.equals(s) ? 1 : 0);
          }
          pw.print("  ");
          for(String s : rares) {
              pw.print(sep);
              pw.print(items.contains(s) ? 1 : 0);
          }
          pw.println();
      }
      pw.close();
      L.info("Finished <"+fName+">.");
  }

  @Override
	public FullResult stats(int slot, Summarizer out, HackFilter... filters)
      throws Exception
	{
      return stats(slot, out, null, filters);
  }

  @Override
	public FullResult stats(int slot, Summarizer out, FullResult reference, HackFilter... filters)
      throws Exception
	{
		Map<String,Integer> types = new HashMap<>();
		Map<String,Integer> basics = new HashMap<>();
		Map<Integer,Integer> nkeys = new HashMap<>();
		Map<Integer,Integer> levels = new HashMap<>();
		Map<String,Integer> levelsPM = new HashMap<>();
		Map<String,Integer> levelsENE = new HashMap<>();
		Map<Integer,Integer> levelTotals = new HashMap<>();
		Map<Integer,Integer> counts = new HashMap<>();
		Map<Integer,Integer> noOfItems = new HashMap<>();
		Map<Integer,Integer> noOfResos = new HashMap<>();
		Map<Integer,Integer> noOfXmps = new HashMap<>();
		Map<Integer,Integer> noOfOtherNoKAM = new HashMap<>();
		Map<String,Integer> noOfPattern = new HashMap<>();
		Map<String,Integer> hackLevelPatterns = new HashMap<>();
		Map<String,Integer> hackLevelPatternsRESO = new HashMap<>();
		Map<String,Integer> hackLevelPatternsXMP = new HashMap<>();
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
		Map<Integer,Integer> getKeysStatsHas = new HashMap<>();
		Map<Integer,Integer> getKeysStatsHasnot = new HashMap<>();
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
      List<HackItem> items = hackResult.hack.result(slot);
		  if ( items != null ) for(HackItem hackItem : items ) {
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
      if ( !INCL_KEY_AND_MEDIA ) {
          notSeenItems.remove(KEY);
          notSeenItems.remove(MEDIA);
      }
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
      List<HackItem> items = hackResult.hack.result(slot);
		  if ( items != null ) for(HackItem hackItem : items ) {
        int count = hackItem.quantity;
        sumCount += count;
        increment(basics, "Items", 1);
        increment(counts, count, 1);
        String shortName = shortItemName(hackItem);
        if ( INCL_KEY_AND_MEDIA || !isKAM(hackItem) ) increment(types, shortName, count);
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
          fullItem += "."+relLevel;
          relLevelCount++;
          relLevelSum += relLevel;
          if ( !CUBE.equals(hackItem.object) ) {
            relLevelCountNPC++;
            relLevelSumNPC += relLevel;
            increment(hackLevelPatterns, hackLevel+":"+hackItem.level, count);
          }
          increment(levels, relLevel, count);
          increment(levelsPM, relLevel < 0 ? "-" : relLevel == 0 ? "=" : "+", count);
          increment(levelsENE, relLevel == 0 ? "=" : "!=", count);
          increment(levelPattern, levelBase+relLevel, count);
          if ( RESO.equals(hackItem.object) ) {
              increment(hackLevelPatternsRESO, hackLevel+":"+hackItem.level, count);
              resoPattern[relLevel+1] = count;
          }
          else if ( XMP.equals(hackItem.object) ) {
              increment(hackLevelPatternsXMP, hackLevel+":"+hackItem.level, count);
              xmpPattern[relLevel+1] = count;
          }
        }
        else if ( isOther(hackItem) ) {
          sumOtherCount += count;
        }
        if ( hackLevel > 1 && hackLevel < 7 ) {
            for(int i = -1; i < 3; i++) {
                increment(levelResults26, i, hackLevelSum[i+hackLevel]);
            }
        }
        notSeenItems.remove(fullItem);
        if ( INCL_KEY_AND_MEDIA || !isKAM(hackItem) ) increment(crossItems, fullItem, count);
      }
      for(String noItem : notSeenItems) increment(crossItems, noItem, 0);
      increment(noOfResos, sumResoCount, 1);
      increment(noOfXmps, sumXmpCount, 1);
      increment(noOfOtherNoKAM, sumOtherCount, 1);
      increment(noOfUSPattern, Integer.toString(sumResoCount) + sumXmpCount + sumUSCount, 1);
      increment(noResoPattern, ia2str(resoPattern), 1);
      increment(noXMPPattern, ia2str(xmpPattern), 1);
      increment(noOfPattern, Integer.toString(sumResoCount) +"/"+ sumXmpCount, 1);
      increment(noOfPatternBig, Integer.toString(sumResoCount) +"/"+ sumXmpCount +"/"+ sumOtherCount, 1);
		  increment(noOfPatternHuge, Integer.toString(sumResoCount) + sumXmpCount + "-" + sumKeyCount + sumShieldCount, 1);
		  increment(noOfItems, sumCount, 1);
      increment(playerLevelVsKeys, hackResult.getPlayerLevel(), sumKeyCount);
      increment(hackLevelVsKeys, hackLevel, sumKeyCount);
      increment(hasKey ? getKeysStatsHas : getKeysStatsHasnot, hackContainsKey ?1:0, 1);
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
    FullResult res = new FullResult(slot == 0 ? null : "Bonus", filters, out);
    out.startColumn(Util.append(new StringBuilder(), filters));
		res.summary("Basics", basics, totalCount, true, reference);
		res.summary("With Key", getKeysStatsHas, totalCountHas, true, reference);
		res.summary("WO Key", getKeysStatsHasnot, totalCountHasnot, true, reference);
		if(longMode == LONG) res.summary("hack levels", levelTotals, totalCount, true, reference);
		if(longMode == LONG) res.summary("Items", noOfItems, totalCount, true, reference);
		res.summary("Resos", noOfResos, totalCount, true, reference);
		res.summary("Xmps", noOfXmps, totalCount, true, reference);
		res.summary("Other (no R,XMP,K,M)", noOfOtherNoKAM, totalCount, true, reference);
		if(longMode == LONG) res.summary("nkeys", nkeys, totalCount, true, reference);
		res.summary("Short Patterns", noOfPattern, totalCount, true, reference);
    // XXX the next 3 also depend on what players actually hacked recently, so true changes are hard to track.
		if(longMode == LONG) res.summary("Hacklevel:Itemlevel RESO+XMP", hackLevelPatterns, totalCount, true, reference);
		if(longMode == LONG) res.summary("Hacklevel:Itemlevel RESO", hackLevelPatternsRESO, totalCount, true, reference);
		if(longMode == LONG) res.summary("Hacklevel:Itemlevel XMP", hackLevelPatternsXMP, totalCount, true, reference);
		if(longMode == LONG) res.summary("US Patterns", noOfUSPattern, totalCount, true, reference);
		res.summary("Rare Items", rareItems, totalCount, true, reference);
		if(longMode == LONG) res.summary("Long Patterns", noOfPatternBig, totalCount, true, reference);
		if(longMode == LONG) res.summary("Huge Patterns", noOfPatternHuge, totalCount, true, reference);
		res.summary("Items by Type", types, totalCount, true, reference);
		res.summary("Items by Level", levels, totalCount, true, reference);
		res.summary("Items by Level2", levelsPM, totalCount, true, reference);
		res.summary("Items by Level3", levelsENE, totalCount, true, reference);
		if(longMode == LONG) res.summary("Patterns of Items by Overlevel, Level", levelPattern, totalCount, true, reference);
		res.summary2("Items x Level", crossItems, totalCount, true, reference);
		if(longMode == LONG) res.summary2("Player Level vs Keys", playerLevelVsKeys, totalCount, true, reference);
		if(longMode == LONG) res.summary2("Hack Level vs Keys", hackLevelVsKeys, totalCount, true, reference);
    /*
    if(longMode == LONG ) {
        for(int i = 1; i <= 8; i++) {
            if ( longMode == LONG || i == 1 || i == 7 || i == 8 ) res.summary("Hack Level L"+i, levelResults.getRow(i), levelCounts.get(i), false, reference);
            if ( longMode != LONG && i == 2 ) {
                res.summary2("Hack Level L2-6 rel.", levelResults26, totalCount, true, reference);
            }
        }
		}
    */
    for(int i = longMode == LONG ? 1 : 5; i <= 8; i++) {
        res.summary("Hack Level L"+i, levelResults.getRow(i), levelCounts.get(i), true, reference);
    }
		if(longMode == LONG) res.summary("Hackers", hackers, totalCount, true, reference);
		if(longMode == LONG) res.summary(WEEKS, weeks, totalCount, true, reference);
		if(longMode == LONG) res.summary("ResoPatterns", noResoPattern, totalCount, true, reference);
		if(longMode == LONG) res.summary("XMPPatterns", noXMPPattern, totalCount, true, reference);
		out.value("overHacking-Correlation", overHacks.correlation());
		out.value("overHacking-NonPC-Correlation", overHacksNPC.correlation());
    out.endColumn();
    return res;
  }

  public static boolean isKAM(HackItem hackItem) 
  {
      return KEY.equals(hackItem.object) || MEDIA.equals(hackItem.object);
  } 

  public static boolean isOther(HackItem hackItem) 
  {
      return !NOOTHER.contains(hackItem.object);
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

  public List<FullResult> runFilterStack(int slot, int longMode, Summarizer o)
      throws Exception
  {
    o.value("Last Hack", String.format("%tF %<tT", 1000*(long) endTime));
    o.value("Total Data", allHacks.size());
    List<FullResult> res = new ArrayList<FullResult>();
    if ( longMode == LONG ) {
        FullResult base1 = stats(slot, o, NO_FILTER);
        res.add(base1);
        for(HackFilter f0 : FRIEND_OR_FOE) {
            FullResult res2 = stats(slot, o, f0);
            res.add(res2);
        }
    }
    // Bootstrapping for changes within latest period
    LaterThanFilter timeFilter0 = createPercTimeFilter(0.33, times[0], FRIEND_FILTER);
    LaterThanFilter timeFilter1 = createPercTimeFilter(0.33, times[0], FOE_FILTER);
    FullResult res10 = stats(slot, Summarizer.NO_SUMMARIZER, times[0], new Not(timeFilter0), FRIEND_FILTER);
    FullResult res11 = stats(slot, Summarizer.NO_SUMMARIZER, times[0], new Not(timeFilter1), FOE_FILTER);
    FullResult res00 = stats(slot, o, res10, timeFilter0, FRIEND_FILTER);
    FullResult res01 = stats(slot, o, res11, timeFilter1, FOE_FILTER);
    // Finally add some of the bootstrap data
    res.add(timeFilter0.compareTo(timeFilter1) < 0 ? res01 : res00);
    res.add(timeFilter0.compareTo(timeFilter1) < 0 ? res00 : res01);
    // Loop over the time events
    for(int time = 0; time < times.length; time++) {
        final DateFilter timeFilter = times[time];
        final DateFilter pastTimeFilter = time + 1 < times.length ? times[time+1] : null;
        StatCreator st2 = new StatCreator() {
                @Override
                public FullResult stats(int slot, Summarizer out, HackFilter... filters) throws Exception {
                    HackFilter[] combinedFilters = new HackFilter[filters.length+1];
                    System.arraycopy(filters, 0, combinedFilters, 1, filters.length);
                    FullResult referenceResult = null;
                    if ( pastTimeFilter != null ) {
                        combinedFilters[0] = pastTimeFilter;
                        referenceResult = Phase1.this.stats(slot, Summarizer.NO_SUMMARIZER, combinedFilters);
                    }
                    combinedFilters[0] = timeFilter;
                    return Phase1.this.stats(slot, out, referenceResult, combinedFilters);
                }

                @Override
                public FullResult stats(int slot, Summarizer out, FullResult reference, HackFilter... filters) throws Exception {
                    throw new RuntimeException("Method not implemented.");
                }
            };
        if ( longMode == LONG ) {
            FullResult res1 = st2.stats(slot, o);
            res.add(res1);
        }
        for(HackFilter f0 : FRIEND_OR_FOE) {
            FullResult res2 = st2.stats(slot, o, f0);
            res.add(res2);
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, R8_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, R8_FILTER, NON_P8_FILTER ));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL1_FILTER));
            }
            if(longMode != LONG && time == 0 ) {
                res.add(st2.stats(slot, o, f0, L26_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL2_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL3_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL4_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL5_FILTER));
            }
            if(longMode == LONG) {
                res.add(st2.stats(slot, o, f0, HL6_FILTER));
            }
            if(longMode == LONG || time == 0 ) {
                res.add(st2.stats(slot, o, f0, HL7_FILTER));
            }
            if(longMode == LONG || time == 0 ) {
                res.add(st2.stats(slot, o, f0, HL8_FILTER));
            }
        }
    }
    return res;
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
		for(String arg : args) p1.add(arg);
    p1.dumpCSV("out_o.csv","; ", FRIEND_FILTER);
    p1.dumpCSV("out_e.csv","; ", FOE_FILTER);
    CombinedSummarizer o;
    o = new CombinedSummarizer();
    o.addSummarizer(new XSLTSummarizer());
    o.addSummarizer(new XSLTSummarizer("layout1.xsl", "out.html"));
    List<FullResult> res = p1.runFilterStack(0, longMode, o);
    o.close();
    // 
    o = new CombinedSummarizer();
    o.addSummarizer(new XSLTSummarizer("layout1.xsl", "out_bonus.html"));
    List<FullResult> resBonus = p1.runFilterStack(1, SHORT, o);
    o.close();
  }

}
