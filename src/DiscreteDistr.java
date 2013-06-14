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
        if ( delta <= 0 ) {
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
        double sum = 0.0;
        Set<T> keys = new HashSet<T>();
        keys.addAll(keySet());
        keys.addAll(d2.keySet());
        for(T t : keys) {
            double o = d2.getRaw(t);
            if ( o > 0.0 ) sum += o * Math.log(o/get(t));
        }
        return sum;
    }

    public static double gtest(DiscreteDistr d1, DiscreteDistr d2)
    {
        throw new RuntimeException("Not yet implemented!");
        //will be 2 * sum * (entropy(rowSums) + entropy(collSums) - entropy(k))
    }

    public static double entropy(int[][] k) {
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

    public static double entropy(int[] k) {
        double h = 0;
        long sum = sum(k);
        for (int i = 0; i < k.length; i++) if (k[i] != 0) {
            h -= h(k[i], sum);
        }
        return h;
    }

    public static double h(int c, long sum) { 
        if ( c == 0 ) return 0.0;
        double p = (double) c / sum;
        return p * Math.log(p);
    }

    public static long sum(int[] k) {
        long sum = 0;
        for (int i = 0; i < k.length; i++) sum += k[i];
        return sum;
    }

}
