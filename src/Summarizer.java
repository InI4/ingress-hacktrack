package de.spieleck.ingress.hackstat;

/**
 * Output interface.
 * Contract is meant to call
 * (startColumn,(start, description, setNorms, item x n, finish) x m) x o, close
 */
public interface Summarizer
{
    public void startColumn(CharSequence label) throws Exception;
    public void endColumn() throws Exception;
    public void start(CharSequence label) throws Exception;
    public void description(CharSequence desc) throws Exception;
    public void setNorms(double f, double f2);
    public void item(Object key, int h) throws Exception;
    public void item(Object key, Stats1D h) throws Exception;
    public void finish(int sum) throws Exception;
    public void value(CharSequence label, double value) throws Exception;
    public void value(CharSequence label, CharSequence value) throws Exception;
    public void value(CharSequence label, long value) throws Exception;
    public void close() throws Exception;
}
