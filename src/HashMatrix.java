package de.spieleck.ingress.hackstat;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;

public class HashMatrix<X,Y,V>
{
    protected HashMap<X,HashMap<Y,V>> store = new HashMap<>();
    protected HashSet<X> rows = new HashSet<>();
    protected HashSet<Y> cols = new HashSet<>();

    public HashMatrix()
    {
    }

    public void put(X row, Y col, V value)
    {
        rows.add(row);
        cols.add(col);
        HashMap<Y,V> rowMap = store.get(row);
        if ( rowMap == null ) {
            rowMap = new HashMap<Y,V>();
            store.put(row, rowMap);
        }
        rowMap.put(col, value);
    }

    public V get(X row, Y col)
    {
        HashMap<Y,V> rowMap = store.get(row);
        if ( rowMap == null ) return null;
        return rowMap.get(col);
    }

    public Map<Y,V> getRow(X row)
    {
        HashMap<Y,V> rowMap = store.get(row);
        if ( rowMap == null ) return null;
        return Collections.unmodifiableMap(rowMap);
    }

    public Set<X> getRows()
    {
        return Collections.unmodifiableSet(rows);
    }

    public Set<Y> getColumns()
    {
        return Collections.unmodifiableSet(cols);
    }
}
