package de.spieleck.ingress.hackstat;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public interface HackFilter
    extends Globals
{
    public boolean accept(HackResult hr);

    /* Below are many handy implementations of filters. */

    public final static HackFilter NO_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) { return true; }
            public String toString() { return ""; }
        };

    public final static HackFilter FRIEND_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) { return FRIEND.equals(hr.hack.type); }
            public String toString() { return "FRIEND"; }
        };

    public final static HackFilter FOE_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) { return FOE.equals(hr.hack.type); }
            public String toString() { return "FOE"; }
        };

    public final static HackFilter NEUTRAL_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) { return NEUTRAL.equals(hr.hack.type); }
            public String toString() { return "NEUTRAL"; }
        };

    public final static HackFilter[] FRIEND_OR_FOE = new HackFilter[]{ FRIEND_FILTER, FOE_FILTER, NEUTRAL_FILTER};

    public final static HackFilter R8_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) {
                for(int reso : hr.resos) {
                    if ( reso == 8 ) return true;
                }
                return false;
            }
            public String toString() { return "R8"; }
        };

    public final static HackFilter NON_P8_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) {
                for(int reso : hr.resos) {
                    if ( reso < 8 ) return true;
                }
                return false;
            }
            public String toString() { return "NON_P8"; }
        };

    public abstract static class HLX_Filter
        implements HackFilter
    {
        public abstract int level();

        public final boolean accept(HackResult hr) {
            int l = level();
            if ( hr.hacker.level < l ) return false;
            int rSum = 0;
            for(int reso : hr.resos) rSum += reso;
            int pLevel = Math.max(1, rSum / 8);
            return Math.min(pLevel, hr.hacker.level) == l;
        }
        public final String toString() { return "HL"+level(); }
    }

    public final static SimpleDateFormat DF = new SimpleDateFormat("yy-MM-dd");

    public abstract static class DateFilter
        implements HackFilter
    {
        protected long date;
        protected String dateStr;

        public DateFilter(String dateStr)
            throws ParseException
        {
            this.dateStr = dateStr;
            date = DF.parse(dateStr).getTime() / 1000L;
        }

        public abstract boolean accept(HackResult hr); 
       
        public abstract String toString();
    }

    public static class LaterThanFilter
        extends DateFilter
    {
        public LaterThanFilter(String dateStr) throws ParseException
        {
            super(dateStr);
        }
        public boolean accept(HackResult hr) { return hr.timestamp > date; }
        public String toString() { return ">"+dateStr; }
    }

    public static class BeforeThanFilter
        extends DateFilter
    {
        public BeforeThanFilter(String dateStr) throws ParseException
        {
            super(dateStr);
        }
        public boolean accept(HackResult hr) { return hr.timestamp < date; }
        public String toString() { return "<"+dateStr; }
    }

    public static class BetweenDateFilter
        extends And
    {
        protected String s;
        public BetweenDateFilter(String date1, String date2) throws ParseException
        {
            super(new LaterThanFilter(date1), new BeforeThanFilter(date2));
            s = date1+":"+date2;
        }
        public String toString() { return s; }
    }

    public final static HackFilter HL1_FILTER = new HLX_Filter() {
        public int level() { return 1; }
    };

    public final static HackFilter HL2_FILTER = new HLX_Filter() {
        public int level() { return 2; }
    };

    public final static HackFilter HL3_FILTER = new HLX_Filter() {
        public int level() { return 3; }
    };

    public final static HackFilter HL4_FILTER = new HLX_Filter() {
        public int level() { return 4; }
    };

    public final static HackFilter HL5_FILTER = new HLX_Filter() {
        public int level() { return 5; }
    };

    public final static HackFilter HL6_FILTER = new HLX_Filter() {
        public int level() { return 6; }
    };

    public final static HackFilter HL7_FILTER = new HLX_Filter() {
        public int level() { return 7; }
    };

    public final static HackFilter HL8_FILTER = new HLX_Filter() {
        public int level() { return 8; }
    };

    public final static HackFilter L26_FILTER = new HackFilter() {
            public boolean accept(HackResult hr) {
            int l = hr.getLevel();
            return (l >= 2) && ( l <= 6 );
            }
            public String toString() { return "HL23456"; }
        };

    public static class Util {
          public static StringBuilder append(StringBuilder sb, HackFilter... filters) {
              return append(sb, Arrays.asList(filters));
          }

          public static StringBuilder append(StringBuilder sb, List<HackFilter> filters) {
              boolean notFirst = false;
              for(HackFilter f : filters) {
                  String s = f.toString();
                  if ( s.length() == 0 ) continue;
                  if ( sb.length() > 0 ) sb.append(' ');
                  notFirst = true;
                  sb.append(s);
              }
              return sb;
          }
    }

    public static class And implements HackFilter {
        private List<HackFilter> filters = new ArrayList<HackFilter>();

        public And(HackFilter... filters) {
            for(HackFilter f : filters) this.filters.add(f);
        }

        public boolean accept(HackResult hr) {
            for(HackFilter f : filters) if ( !f.accept(hr) ) return false;
            return true;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("AND(");
            return Util.append(sb, filters).append(')').toString();
        }
    }

    public static class Or implements HackFilter {
        private List<HackFilter> filters = new ArrayList<HackFilter>();

        public Or(HackFilter... filters) {
            for(HackFilter f : filters) this.filters.add(f);
        }

        public boolean accept(HackResult hr) {
            for(HackFilter f : filters) if ( f.accept(hr) ) return true;
            return false;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("OR(");
            return Util.append(sb, filters).append(')').toString();
        }
    }

    public static class Not implements HackFilter {
        private HackFilter filter;

        public Not(HackFilter filter)
        {
            this.filter = filter;
        }

        public boolean accept(HackResult hr)
        {
            return !filter.accept(hr);
        }

        public String toString()
        {
            return "OR("+filter+")";
        }
    }

}
