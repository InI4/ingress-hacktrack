package de.spieleck.ingress.hackstat;

/**
 * Some device to attach results to a summarizer.
 */
public interface StatCreator
{
    /** Generate stats. */
    public FullResult stats(int slot, Summarizer out, HackFilter... filters)
        throws Exception;

    /** Generate stats and compare to a reference. */
    public FullResult stats(int slot, Summarizer out, FullResult reference, HackFilter... filters)
          throws Exception;
}
