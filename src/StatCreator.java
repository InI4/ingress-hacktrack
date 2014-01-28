package de.spieleck.ingress.hackstat;

/**
 * Some device to attach results to a summarizer.
 */
public interface StatCreator
{
    /** Generate stats. */
    public FullResult stats(Summarizer out, HackFilter... filters)
        throws Exception;

    /** Generate stats and compare to a reference. */
    public FullResult stats(Summarizer out, FullResult reference, HackFilter... filters)
          throws Exception;
}
