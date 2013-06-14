package de.spieleck.ingress.hackstat;

import java.util.Map;

public class GenericHelper
{
	public static <T> void increment(Map<T, Stats1D> data, T key, double delta)
  {
       Stats1D h = data.get(key);
       if ( h == null ) {
          h = new Stats1D();
          data.put(key, h);
       }
			 h.add(delta);
  }

	public static <T> Integer increment(Map<T, Integer> data, T key, int delta)
  {
       Integer h = inc(data.get(key), delta);
			 data.put(key, h);
       return h;
  }

  public static Integer inc(Integer h, int delta)
  {
      if ( h == null ) {
          return Integer.valueOf(delta); 
      } else {
          return Integer.valueOf(delta + h.intValue());
      }
  }
}
