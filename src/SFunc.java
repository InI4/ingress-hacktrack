package de.spieleck.ingress.hackstat;

/**
    The following Java functions for calculating normal and
    chi-square probabilities and critical values were adapted by
    John Walker from C implementations
    written by Gary Perlman of Wang Institute, Tyngsboro, MA 01879. 
    found here: 
    http://www.fourmilab.ch/rpkp/experiments/analysis/chiCalc.html
    http://www.fourmilab.ch/rpkp/experiments/analysis/chiCalc.js

    All the original C code, the JavaScript and this is in the public domain. 
*/

public class SFunc
{
    /** Maximum meaningful z value */
    public final static double Z_MAX = 6.0;

    /** log(sqrt(pi)) */
    public final static double LOG_SQRT_PI = 0.5723649429247000870717135;

    /** 1 / sqrt(pi) */
    public final static double I_SQRT_PI = 0.5641895835477562869480795; 

    /** Accuracy of critchi approximation */
    public final static double CHI_EPSILON = 0.000001;

    /** Maximum chi-square value */
    public final static double CHI_MAX = 99999.0;   

    public final static double BIGX = 20.0;

    /**
        POZ  --  probability of normal z value.

        Adapted from a polynomial approximation in:
                Ibbetson D, Algorithm 209
                Collected Algorithms of the CACM 1963 p. 616
        Note:
                This routine has six digit accuracy, so it is only useful for absolute
                z values < 6.  For z values >= to 6.0, poz() returns 0.0.
    */
    public static double poz(double z) {
        double y, x;
        if (z == 0.0) {
            x = 0.0;
        } else {
            y = 0.5 * Math.abs(z);
            if (y >= (Z_MAX * 0.5)) {
                x = 1.0;
            } else if (y < 1.0) {
                double w = y * y;
                x = ((((((((0.000124818987 * w
                         - 0.001075204047) * w + 0.005198775019) * w
                         - 0.019198292004) * w + 0.059054035642) * w
                         - 0.151968751364) * w + 0.319152932694) * w
                         - 0.531923007300) * w + 0.797884560593) * y * 2.0;
            } else {
                y -= 2.0;
                x = (((((((((((((-0.000045255659 * y
                               + 0.000152529290) * y - 0.000019538132) * y
                               - 0.000676904986) * y + 0.001390604284) * y
                               - 0.000794620820) * y - 0.002034254874) * y
                               + 0.006549791214) * y - 0.010557625006) * y
                               + 0.011630447319) * y - 0.009279453341) * y
                               + 0.005353579108) * y - 0.002141268741) * y
                               + 0.000535310849) * y + 0.999936657524;
            }
        }
        return z > 0.0 ? ((x + 1.0) * 0.5) : ((1.0 - x) * 0.5);
    }

    /**
            POCHISQ  --  probability of chi-square value.

              Adapted from:
                      Hill, I. D. and Pike, M. C.  Algorithm 299
                      Collected Algorithms for the CACM 1967 p. 243
              Updated for rounding errors based on remark in
                      ACM TOMS June 1985, page 185
    */

    public static double pochisq(double x, int df) {
        
        if (x <= 0.0 || df < 1) {
            return 1.0;
        }
        
        double a = 0.5 * x;
        boolean even = df % 2 == 0;
        double y = df > 1 ? Math.exp(-a) : 0.0;
        double s = even ? y : 2.0 * poz(-Math.sqrt(x));
        if ( df <= 2 ) {
            return s;
        } 
        else  {
            x = 0.5 * (df - 1);
            double z = (even ? 1.0 : 0.5);
            if (a > BIGX) {
                double e = even ? 0.0 : LOG_SQRT_PI;
                double c = Math.log(a);
                s = 0.0;
                while (z <= x) {
                    e += Math.log(z);
                    s += Math.exp(c * z - a - e);
                    z += 1.0;
                }
                return s;
            } else {
                double e = even ? 1.0 : I_SQRT_PI / Math.sqrt(a);
                double c = 0.0;
                while (z <= x) {
                    e = e * (a / z);
                    c = c + e;
                    z += 1.0;
                }
                return c * y + s;
            }
        }
    }

    /**
        CRITCHI  --  Compute critical chi-square value to.
                     produce given p.  We just do a bisection
                     search for a value within CHI_EPSILON,
                     relying on the monotonicity of pochisq().  */
    public static double critchi(double p, int df)
    {
        if (p <= 0.0) {
            return CHI_MAX;
        } else {
            if (p >= 1.0) {
                return 0.0;
            }
        }
        
        /*
        double chisqval = 0.5 * df / Math.sqrt(p);    // fair first value 
        double minchisq = 0.0;
        double maxchisq = CHI_MAX;
        while ((maxchisq - minchisq) > CHI_EPSILON) {
System.out.println(" ** "+chisqval);          
            if (pochisq(chisqval, df) < p) {
                maxchisq = chisqval;
            } else {
                minchisq = chisqval;
            }
            chisqval = 0.5 * (maxchisq + minchisq);
        }
        */
        double h = (df - 0.7)/Math.sqrt(p);
        double c1 =   h, d1 = pochisq(c1, df) - p;
        double c2 = 0.0, d2 = pochisq(c2, df) - p;
        int l = 0;
        while ( Math.abs(c1 - c2) > CHI_EPSILON ) {
            double c = (d1 * c2 - d2 * c1)/(d1 - d2);
            double d = pochisq(c, df) - p;
// System.out.println(" ** "+c1+" "+c2+" "+d1+" "+d2+" || "+c+" "+d);          
            if ( d * d1 < 0.0 ) {
                c2 = c; d2 = d;
                if ( l == 1 ) d1 /= 2.0; else l = 1;
            } else {
                c1 = c; d1 = d;
                if ( l == 2 ) d2 /= 2.0; else l = 2;
            }
        }
        return 0.5*(c1+c2);
    }

    public static void main(String[] args)
    {
        System.out.println("poz(1/2)="+poz(0.5));
        System.out.println("pochisq(1/2, 7)="+pochisq(0.5,7));
        System.out.println("critchi(0.95, 1)="+critchi(0.95, 1));
        System.out.println("critchi(0.95, 2)="+critchi(0.95, 2));
        System.out.println("critchi(0.95, 3)="+critchi(0.95, 3));
        System.out.println("critchi(0.95, 4)="+critchi(0.95, 4));
        System.out.println("critchi(0.95, 5)="+critchi(0.95, 5));
        System.out.println("critchi(0.95, 11)="+critchi(0.95, 11));
        System.out.println("critchi(0.95, 23)="+critchi(0.95, 23));
    }
}    
