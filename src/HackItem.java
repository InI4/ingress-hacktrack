package de.spieleck.ingress.hackstat;

public class HackItem
    implements Globals
{
		String object;
		int level;
		int quantity;
    String rarity;

    public boolean hasRarity() { return rarity != null && rarity.length() != 0; }

    public String toString()
    {
        String s;
        switch(object) {
          case KEY: s = "KY"; break;
          case CUBE: s = "C"+level; break;
          case RESO: s = "R"+level; break;
          case XMP: s = "X"+level; break;
          case SHIELD: s = rareString("S", "H"); break;
          case FORCE_AMP: s = rareString("F", "A"); break;
          case HEAT_SINK: s = rareString("H", "S"); break;
          case LINK_AMP: s = rareString("L", "A"); break;
          case MULTI_HACK: s = rareString("M", "A"); break;
          case TURRET: s = rareString("T", "A"); break;
          default:
            s = object;
            if ( level > 0 ) s += level;
        }
        if ( quantity > 1 ) s += "x"+quantity;
        return s;
    }

    private String rareString(String prefix, String def)
    {
        if ( !hasRarity() ) return prefix + def;
        else return prefix + rarity.charAt(0);
    }
}

