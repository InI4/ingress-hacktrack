package de.spieleck.ingress.hackstat;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import static de.spieleck.ingress.hackstat.GenericHelper.*;
import static de.spieleck.ingress.hackstat.HackFilter.*;

public class Phase1
    implements Globals
{
	private List<HackResult> allHacks = new ArrayList<HackResult>();

  private final DataParser parser = new DataParser(true);

  private final static int SHORT = 0;
  private final static int ADAPTIVE = 1;
  private final static int LONG = 2;

  private int longMode;

	public Phase1(int longMode)
	{
      this.longMode = longMode;
	}

	public void add(File fi)
		throws IOException
	{
		FileReader fr = new FileReader(fi);
		List<HackResult> d = parser.grok(fi);
    for(HackResult hackResult : d) {
        int hackLevel = hackResult.getLevel();
        for(HackItem hackItem : hackResult.hack.items) {
            if ( CUBE.equals(hackItem.object) && hackItem.level != hackLevel ) plausi("1", fi, hackItem, hackResult);
            else if ( hackItem.level  > 0 && hackItem.level > hackLevel+2 ) plausi("2", fi, hackItem, hackResult);
            else if ( hackItem.level  > 0 && hackItem.level < hackLevel-1 ) plausi("3", fi, hackItem, hackResult);
        }
    }
		allHacks.addAll(d);
		System.out.println("***** "+allHacks.size());
	}

  private static void plausi(String mark, File fi, HackItem item, HackResult hack)
  {
      System.err.println(String.format("WARN=%s@%-36s %-4s %s",mark,fi+"."+hack._id,item,hack));
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
      System.err.println("Finished <"+fName+">.");
  }

	public FullResult stats(Summarizer out, HackFilter... filters)
      throws Exception
	{
		Map<String,Integer> types = new HashMap<>();
		Map<Integer,Integer> levels = new HashMap<>();
		Map<Integer,Integer> counts = new HashMap<>();
		Map<Integer,Integer> noOfItems = new HashMap<>();
		Map<Integer,Integer> noOfResos = new HashMap<>();
		Map<Integer,Integer> noOfXmps = new HashMap<>();
		Map<Integer,Integer> noOfOther = new HashMap<>();
		Map<String,Integer> noOfPattern = new HashMap<>();
		Map<String,Integer> noOfPatternBig = new HashMap<>();
		Map<String,Integer> noOfPatternHuge = new HashMap<>();
		Map<String,Integer> levelPattern = new HashMap<>();
		IMatrix<Integer,Integer> levelResults = new IMatrix<>();
		Map<Integer,Integer> levelCounts = new HashMap<>();
		Map<String,Stats1D> crossItems = new HashMap<>();
		Map<Integer,Stats1D> playerLevelVsKeys = new HashMap<>();
		Map<Integer,Stats1D> hackLevelVsKeys = new HashMap<>();
		Stats2D overHacks = new Stats2D();
		Stats2D overHacksNPC = new Stats2D();
		int totalCount = 0;
    // XXX Stupid duplication of code to determine all possible item types to count zeros!!
    Set<String> allFullItems = new HashSet<>();
outerloop1:    
		for(HackResult hackResult : allHacks) {
		  int hackLevel = hackResult.getLevel();
		  for(HackFilter fi : filters) {
			  if ( !fi.accept(hackResult) ) continue outerloop1;
		  }
		  for(HackItem hackItem : hackResult.hack.items) {
        String fullItem = hackItem.object;
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
		  totalCount++;
		  int sumCount = 0;
		  int sumResoCount = 0;
		  int sumXmpCount = 0;
		  int sumOtherCount = 0;
		  int sumKeyCount = 0;
		  int sumShieldCount = 0;
		  int sumCubeCount = 0;
		  String levelBase = Integer.toString(hackResult.getOverLevel());
		  int relLevelCount = 0;
		  int relLevelSum = 0;
		  int relLevelCountNPC = 0;
		  int relLevelSumNPC = 0;
		  for(HackItem hackItem : hackResult.hack.items) {
        int count = hackItem.quantity;
        sumCount += count;
        increment(counts, count, 1);
        increment(types, hackItem.object, count);
        switch ( hackItem.object ) {
          case RESO: sumResoCount += hackItem.quantity; break;
          case XMP: sumXmpCount += hackItem.quantity; break;
          case KEY: sumKeyCount += hackItem.quantity; break;
          case SHIELD: sumShieldCount += hackItem.quantity; break;
          case CUBE: sumCubeCount += hackItem.quantity; break;
        }
        String fullItem = hackItem.object;
        increment(levelCounts, hackLevel, 1);
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
          }
          increment(levels, relLevel, count);
          increment(levelPattern, levelBase+relLevel, count);
        }
        else {
          sumOtherCount += count;
        }
        notSeenItems.remove(fullItem);
        increment(crossItems, fullItem, count);
      }
      for(String noItem : notSeenItems) increment(crossItems, noItem, 0);
      increment(noOfResos, sumResoCount, 1);
      increment(noOfXmps, sumXmpCount, 1);
      increment(noOfOther, sumOtherCount, 1);
      increment(noOfPattern, Integer.toString(sumResoCount) + sumXmpCount, 1);
      increment(noOfPatternBig, Integer.toString(sumResoCount) + sumXmpCount + sumOtherCount, 1);
		  increment(noOfPatternHuge, Integer.toString(sumResoCount) + sumXmpCount + "-" + sumKeyCount + sumShieldCount, 1);
		  increment(noOfItems, sumCount, 1);
      increment(playerLevelVsKeys, hackResult.getPlayerLevel(), sumKeyCount);
      increment(hackLevelVsKeys, hackLevel, sumKeyCount);
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
    FullResult res = new FullResult(out);
    out.startColumn(Util.append(new StringBuilder(), filters));
		res.summary("Items", noOfItems, totalCount);
		res.summary("Resos", noOfResos, totalCount);
		res.summary("Xmps", noOfXmps, totalCount);
		res.summary("Other", noOfOther, totalCount);
		res.summary("Short Patterns", noOfPattern, totalCount);
		if(longMode == LONG) res.summary("Long Patterns", noOfPatternBig, totalCount);
		if(longMode == LONG) res.summary("Huge Patterns", noOfPatternHuge, totalCount);
		res.summary("Items by Type", types, totalCount);
		res.summary("Items by Level", levels, totalCount);
		if(longMode == LONG) res.summary("Patterns of Items by Overlevel, Level", levelPattern, totalCount);
		res.summary2("Items x Level", crossItems, totalCount, true);
		if(longMode == LONG) res.summary2("Player Level vs Keys", playerLevelVsKeys, totalCount, true);
		if(longMode == LONG) res.summary2("Hack Level vs Keys", hackLevelVsKeys, totalCount, true);
		for(int i = 1; i <= 8; i++) {
        res.summary("Hack Level L"+i, levelResults.getRow(i), levelCounts.get(i), false);
		}
		out.value("overHacking-Correlation", overHacks.correlation());
		out.value("overHacking-NonPC-Correlation", overHacksNPC.correlation());
    out.endColumn();
    return res;
  }

  /**
   * Class to collect all data, to allow something like a G-Test for "relevancy".
   * The details might probably be programmed out in a "next" version of the programm,
   * since we might still consider it nice, to have all the data analyzed.
   */
  public class FullResult
  {
      private Summarizer out;
      private HashMap<String,HashMap<Object,Double>> data = new HashMap<>();

      private FullResult(Summarizer out)
      {
          this.out = out;
      }

      private void summary(String label, Map<? extends Object, Integer> data, Integer norm)
          throws Exception
      {
          summary(label, data, norm, true);
      }

      private void summary(String label, Map<? extends Object, Integer> data, Integer norm, boolean average)
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
        if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
        out.description(description);
        out.setNorms(f, f2);
        for(Object key : keys) {
            out.item(key, data.get(key));
            addData(label, key, data.get(key)*f2);
        }
        out.finish(sum);
      }

      private void summary2(String label, Map<? extends Object, Stats1D> data, Integer norm, boolean average)
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
        if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
        out.description(description);
        for(Object key : keys) {
            out.item(key, data.get(key));
            addData(label, key, data.get(key).average());
        }
        out.finish(sum);
      }

      private void addData(String key1, Object key2, double d)
      {
      }
  }

	public static void main(String[] args)
		throws Exception
	{
    int longMode = SHORT;
		Phase1 p1 = new Phase1(longMode);
    Summarizer o;
    o = new XSLTSummarizer();
    o = new AppendableSummarizer(System.out);
    o = new XSLTSummarizer("layout1.xsl");
		for(String arg : args) p1.add(new File(arg));
    p1.dumpCSV("out.csv",";");
    HackFilter[] times = new HackFilter[] {NO_FILTER, new LaterThanFilter("2013-06-02")};
          
    for(HackFilter tFilter : times) {
        p1.stats(o, tFilter);
        for(HackFilter f0 : FRIEND_OR_FOE) {
            p1.stats(o, tFilter, f0);
            if(longMode == LONG) p1.stats(o, tFilter, f0, R8_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, R8_FILTER, NON_P8_FILTER );
            p1.stats(o, tFilter, f0, HL1_FILTER);
            if(longMode != LONG) p1.stats(o, tFilter, f0, L26_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, HL2_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, HL3_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, HL4_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, HL5_FILTER);
            if(longMode == LONG) p1.stats(o, tFilter, f0, HL6_FILTER);
            p1.stats(o, tFilter, f0, HL7_FILTER);
            p1.stats(o, tFilter, f0, HL8_FILTER);
        }
    }
    o.close();
	}
}
