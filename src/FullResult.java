package de.spieleck.ingress.hackstat;

import java.util.Set;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import static de.spieleck.ingress.hackstat.HackFilter.Util;

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

    public FullResult(HackFilter[] filters, Summarizer out)
    {
        this.filters = filters;
        this.out = out;
    }

    public HackFilter[] getFilters() { return filters; }

    public Set<String> keys() { return allData.keySet(); }

    public DiscreteDistr get(String key) { return allData.get(key); }

    public void summary(String label, Map<? extends Object, Integer> data, Integer norm, boolean average, FullResult reference)
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
      double f = 100.0 / norm;
      double f2 = 100.0 / sum;
      //
      out.start(label);
      String description = String.format("total=%d, norm=%d ", sum, norm);
      out.description(description);
      if ( allInt && average && sum > 1 ) {
          double hsum = 0.0, hsum2 = 0.0;
          for(Object key : keys) {
              int w = data.get(key);
              hsum += w * ((Integer)key).intValue();
          }
          double avg = hsum/sum;
          for(Object key : keys) {
              double w = data.get(key) - avg;
              hsum2 += w * w * ((Integer)key).intValue();
          }
          out.value("_average", avg);
          out.value("_sdev", Math.sqrt(hsum2/(sum-1)/sum));
      }
      out.setNorms(f, f2);
      DiscreteDistr distr = prepareDistr(label);
      for(Object key : keys) {
          out.item(key, data.get(key));
          distr.inc(key, (int) data.get(key));
      }
      addTest2Out(distr, label, reference);
      out.finish(sum);
    }

    public void summary2(String label, Map<? extends Object, Stats1D> data, Integer norm, boolean average, FullResult reference)
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
      out.start(label);
      String description = String.format("total=%d, norm=%d ", sum, norm);
      out.description(description);
      if ( allInt && average && sum > 1 ) {
          double hsum = 0.0, hsum2 = 0.0;
          for(Object key : keys) {
              double w = data.get(key).sum();
              hsum += w * ((Integer)key).intValue();
          }
          double avg = hsum/sum;
          for(Object key : keys) {
              double w = data.get(key).sum() - avg;
              hsum2 += w * w * ((Integer)key).intValue();
          }
          out.value("_average", avg);
          out.value("_sdev", Math.sqrt(hsum2/(sum-1)/sum));
      }
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
        if ( d2 == null ) return;
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

    public void combinedKeys(FullResult res)
    {
        compareDist(this, res);
    }

    public static void compareDist(FullResult base, FullResult res)
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
}

