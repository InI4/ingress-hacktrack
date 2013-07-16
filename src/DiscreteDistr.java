package de.spieleck.ingress.hackstat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class DiscreteDistr<T>
{
    private int total = 0;
    private TObjectIntMap<T> raw = new TObjectIntHashMap<T>();

    public DiscreteDistr()
    {
    }

    public int inc(T key)
    {
        return incInternal(key, 1);
    }

    public int inc(T key, int delta)
    {
        if ( delta < 0 ) {
            // we could do some smart (checked) updating here.
            throw new RuntimeException("Negative increment not allowed.");
        }
        return incInternal(key, delta);
    }

    private int incInternal(T key, int delta)
    {
        total += delta;
        return raw.adjustOrPutValue(key, delta, delta);
    }

    public T[] keys()
    {
        T[] res = (T[]) raw.keys();
        Arrays.sort(res);
        return res;
    }

    public Set<T> keySet()
    {
        return Collections.unmodifiableSet(raw.keySet());
    }

    public int getSize() { return raw.size(); }

    public int getTotal() { return total; }

    public int getRaw(T key) { return raw.get(key); }

    public double get(T key) { return 1.0 / total * raw.get(key); }

    public double getPerc(T key) { return 100.0 / total * raw.get(key); }

    /**
     * Compares, if d2 is similar to the current distribution.
     * Note, this is not the symmetric test, if two such distributions coincide.
     */
    public double gtest(DiscreteDistr d2)
    {
        return gtest(d2, false);
    }

    /**
     * Compares, if d2 is similar to the current distribution.
     * Note, this is not the symmetric test, if two such distributions coincide.
     * Allows turning on Yates correction. Note this can result in negative, i.e. useless
     * Test results.
     */
    public double gtest(DiscreteDistr d2, boolean yatesCorrection)
    {
        double sum = 0.0;
        Set<T> keys = combinedKeys(d2);
        double d2Total = d2.getTotal(); 
        for(T t : keys) {
            double seen = d2.getRaw(t);
            double expected = d2Total * get(t);
            if ( yatesCorrection ) {
                // Yates continuity correction, adjust seen 0.5 towards expectation.
                if (expected + 0.5 < seen)
                  seen -= 0.5;
                else if (expected - 0.5 > seen)
                  seen += 0.5;
                else
                  seen = expected;
            }
            if ( seen == 0 ) continue;
            sum += seen * Math.log(seen/expected);
        }
        return 2.0 * sum;
    }

    public Set<T> combinedKeys(DiscreteDistr d2)
    {
        Set<T> res = new HashSet<T>();
        res.addAll(keySet());
        if ( d2 != null ) res.addAll(d2.keySet());
        return res;
    }

    /*
    * XXX Junk XXX
    */
    public static <T> double gtest(DiscreteDistr<T> d1, DiscreteDistr<T> d2)
    {
        Set<T> keys = d1.combinedKeys(d2);
        long[] colSums = new long[keys.size()];
        long rowSums1 = 0, rowSums2 = 0;
        long[][] cross = new long[2][keys.size()];
        int i = 0;
        for(T key : keys) {
            long o1 = d1.getRaw(key);
            long o2 = d2.getRaw(key);
            rowSums1 += o1;
            rowSums2 += o2;
            colSums[i] = o1 + o2;
            cross[0][i] = o1;
            cross[1][i] = o2;
        }
        return 2.0 * (rowSums1+rowSums2)
              *(entropy(rowSums1,rowSums2)+entropy(colSums)-entropy(cross));
    }

    public static double entropy(long[][] k) {
        double h = 0;
        long sum = 0;
        for (int i = 0; i < k.length; i++) sum += sum(k[i]);
        for (int i = 0; i < k.length; i++) {
            for (int j = 0; j < k[i].length; j++) {
                h -= h(k[i][j], sum);
            }
        }
        return h;
    }

    public static double entropy(long... k) {
        double h = 0;
        long sum = sum(k);
        for (int i = 0; i < k.length; i++) if (k[i] != 0) {
            h -= h(k[i], sum);
        }
        return h;
    }

    public static double h(long c, long sum) { 
        if ( c == 0 ) return 0.0;
        double p = (double) c / sum;
        return p * Math.log(p);
    }

    public static long sum(long[] k) {
        long sum = 0;
        for (int i = 0; i < k.length; i++) sum += k[i];
        return sum;
    }

}
