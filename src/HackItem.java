package de.spieleck.ingress.hackstat;

public class HackItem
    implements Globals
{
		String object;
		int level;
		int quantity;

    public String toString()
    {
        String s;
        switch(object) {
          case KEY: s = "KY"; break;
          case SHIELD: s = "SH"; break;
          case CUBE: s = "C"+level; break;
          case RESO: s = "R"+level; break;
          case XMP: s = "X"+level; break;
          default:
            s = object;
            if ( level > 0 ) s += level;
        }
        if ( quantity > 1 ) s += "x"+quantity;
        return s;
    }
}

