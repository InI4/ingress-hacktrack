package de.spieleck.ingress.hackstat;

import java.io.IOException;
import java.io.FileReader;
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

    private final boolean verbose;

    public DataParser(boolean verbose)
    {
        this.verbose = verbose;
    }

    public List<HackResult> grok(File fi)
        throws IOException
    {
        FileReader fr = new FileReader(fi);
        _HackContainer o = new _HackContainer();
        Type t = new TypeToken<_HackContainer>(){}.getType();
        o = GSON.fromJson(fr, t);
        L.info("Claimed size="+o.total_rows);
        List<HackResult> res = new ArrayList<HackResult>(o.total_rows);
        int total = 0;
		int count = 0;
        for(_HackRow r : o.rows) {
			++count;
            HackResult h = r.doc;
			h.sourceLine = count; // XXX this does not work, since GSON knows the lines.
			if ( h.resos == null ) {
          L.warn("Skipping item "+count);
          continue;
			}
            if ( verbose ) L.debug(String.format("  %5d %s\n", count, h));
            total += h.getItemCount();
            res.add(h);
        }
        L.info("*** "+res.size()+" hacks for "+total+" items, lengthCheck="+(res.size() != o.total_rows ? "WARNING" : "OK"));
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
