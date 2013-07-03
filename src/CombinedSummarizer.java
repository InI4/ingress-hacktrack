package de.spieleck.ingress.hackstat;

import java.util.List;
import java.util.ArrayList;
/**
 * Collector to add more than one Summary
 */
public class CombinedSummarizer
    implements Summarizer
{
    private List<Summarizer> sums = new ArrayList<Summarizer>();

    public CombinedSummarizer() { };

    public int addSummarizer(Summarizer s)
    {
        sums.add(s);
        return sums.size();
    }

    public void startColumn(CharSequence label) throws Exception {
        for(Summarizer s : sums) s.startColumn(label);
    }
    public void endColumn() throws Exception {
        for(Summarizer s : sums) s.endColumn();
    }
    public void start(CharSequence label) throws Exception {
        for(Summarizer s : sums) s.start(label);
    }
    public void description(CharSequence desc) throws Exception {
        for(Summarizer s : sums) s.description(desc);
    }
    public void setNorms(double f, double f2) {
        for(Summarizer s : sums) s.setNorms(f, f2);
    }
    public void item(Object key, int h) throws Exception {
        for(Summarizer s : sums) s.item(key, h);
    }
    public void item(Object key, Stats1D h) throws Exception {
        for(Summarizer s : sums) s.item(key, h);
    }
    public void finish(int sum) throws Exception {
        for(Summarizer s : sums) s.finish(sum);
    }
    public void value(CharSequence label, double value) throws Exception {
        for(Summarizer s : sums) s.value(label, value);
    }
    public void close() throws Exception {
        for(Summarizer s : sums) s.close();
    }
}
