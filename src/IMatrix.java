package de.spieleck.ingress.hackstat;

import java.util.HashMap;

public class IMatrix<X,Y>
    extends HashMatrix<X,Y,Integer>
{
    public IMatrix()
    {
    }

    public Integer inc(X row, Y col, int delta)
    {
        rows.add(row);
        cols.add(col);
        HashMap<Y,Integer> rowMap = store.get(row);
        if ( rowMap == null ) {
            rowMap = new HashMap<Y,Integer>();
            store.put(row, rowMap);
        }
        return GenericHelper.increment(rowMap, col, delta);
    }
}
