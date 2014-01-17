package de.spieleck.ingress.hackstat;

import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataParser
{
    private final static Logger L = Logger.getLogger(DataParser.class);

    private final static Gson GSON = new Gson();

    public DataParser()
    {
    }

    public List<HackResult> grok(File fi)
        throws IOException
    {
        FileReader fr = new FileReader(fi);
        return grok(fr);
    }

    public List<HackResult> grok(InputStream input)
        throws IOException
    {
        InputStreamReader ir = new InputStreamReader(new BufferedInputStream(input));
        return grok(ir);
    }

    public List<HackResult> grok(Reader reader)
        throws IOException
    {
        long t0 = System.currentTimeMillis();
        _HackContainer o = new _HackContainer();
        Type t = new TypeToken<_HackContainer>(){}.getType();
        o = GSON.fromJson(reader, t);
        if ( o == null ) 
            throw new IOException("GSON returned null!");
        L.info("Claimed size="+o.total_rows);
        if ( o.total_rows == 0 ) 
            throw new IOException("GSON did not find objects!");
        List<HackResult> res = new ArrayList<HackResult>(o.total_rows);
        int total = 0;
        int count = 0;
        int canGetUltraCount = 0;
        for(_HackRow r : o.rows) {
            ++count;
            HackResult h = r.doc;
            h.sourceLine = count; // XXX this does not work, since GSON knows the lines.
            if ( h.resos == null || h.hacker == null ) {
                L.warn("Skipping item "+count+" no resonators attached. id="+h._id);
                continue;
            }
            if(L.isTraceEnabled())L.trace(String.format("  %5d %s\n", count, h));
            if ( h.hasCanGetUltra() ) canGetUltraCount++;
            total += h.getItemCount();
            res.add(h);
        }
        long t1 = System.currentTimeMillis();
        L.info("*** "+res.size()+" hacks for "+total+" items, canGetUltraCount="+canGetUltraCount+", lengthCheck="+(res.size() != o.total_rows ? "WARNING" : "OK")+" dt="+(t1-t0)+" ms");
        return res;
    }

    private final static class _HackContainer
    {
        int total_rows;
        int offset = 0;
        List<_HackRow> rows;
    }

    private final static class _HackRow
    {
        // String id;
        // String key;
        // ??? value
        HackResult doc;
    }

}
