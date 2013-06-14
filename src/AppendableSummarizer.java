package de.spieleck.ingress.hackstat;


import java.io.IOException;

public class AppendableSummarizer
    implements Summarizer
{
    private Appendable a;
    public AppendableSummarizer() 
    {
        this(System.out);
    }

    public AppendableSummarizer(Appendable a)
    {
        this.a = a;
    }

    private CharSequence column;
    private CharSequence label;
    private double f, f2;
    int count = 0;

    public void startColumn(CharSequence column)
        throws IOException
    {
        a.append("\n\n***["+column+"]\n");
        this.column = column;
    }

    public void endColumn() { };

    public void start(CharSequence label)
    {
        this.label = label;
        count = 0;
    }

    public void description(CharSequence desc)
        throws IOException
    {
        a.append(String.format("\n"+column+", "+label+", "+desc+"\n"));
    }

    public void setNorms(double f, double f2)
    {
        this.f = f;
        this.f2 = f2;
    }

    public void item(Object key, int h)
        throws IOException
    {
        a.append(String.format("  %3d. %-12s %7d%8.2f%%", ++count, key, h, f*h));
        if ( f != f2 ) a.append(String.format("%8.2f%%", f2*h));
        a.append('\n');
    }

    public void item(Object key, Stats1D hs)
        throws IOException
    {
        double h = hs.sum();
        double s = hs.sdev();
        double m = 100.0*hs.average();
        double d = 2.0*100.0*hs.sdevAvg();
        a.append(String.format("  %3d. %-12s %7.0f%7.1f%%", ++count,key,h,m));
        a.append(String.format(" [%5.1f,%5.1f]%%", m-d, m+d));
        // if ( f != f2 ) a.append(String.format("%7.1f%%", f2*h));
        a.append('\n');
    }

    public void value(CharSequence msg, double v)
        throws IOException
    {
        a.append(String.format("\n   ====== %s = %9.2g\n", msg, v));
    }

    public void finish(int sum)
        throws IOException
    {
        a.append(String.format("  =========================%7.1f%%\n", f*sum));
    }

    public void close()
        throws IOException
    {
        // we don't keep anything in this summarizer, we are done.
    }

}
