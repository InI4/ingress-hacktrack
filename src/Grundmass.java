package de.spieleck.ingress.hackstat;

import java.util.List;
import java.util.ArrayList;
import java.awt.geom.Point2D;

public class Grundmass
{
    private List<Double> measures = new ArrayList<Double>();

    public Grundmass()
    {
    }

    public int add(Double d)
    {
        measures.add(d);
        return measures.size();
    }

    public void plot()
    {
        final double[] data = new double[measures.size()];
        int n = 0;
        double sum = 0.0;
        double min = 0.0;
        for(Double d0 : measures) {
            double h = d0;
            min = n == 0 ? h : Math.min(h, min);
            data[n++] = h;
            sum += h;
        }
        double av = sum / n;
        System.err.println("av="+av);
        System.err.println("min="+min);
        GOpt1D go = new GOpt1D(av, min/100.0); 
        double x0 = go.opt(new GOpt1D.Function() {
                public double eval(double x) { return -s1(data, x); } }, 
                1e-3);
        System.err.println("x0="+x0);
        for(Point2D.Double p : go) {
            System.out.format("%8.5f %8.5f\n", p.getX(), p.getY());
        }
    }

    public static double s1(double[] data, double base)
    {
        double res = 0.0;
        int nsum = 0;
        for(double v : data) {
            double h0 = v/base;
            int ni = (int) Math.round(v/base);
            nsum += ni;
            double delta = ni*base/v - 1.0;
            res += Math.abs(delta); // delta * delta;
        }
        return res - Math.log(base)/Math.sqrt(data.length); // 0.5 * Math.log(nsum/data.length);
    }

    public static void main(String[] args)
    {
        Grundmass gm = new Grundmass();
        for(String s : args)
        {
            double d = Double.parseDouble(s);
            gm.add(d);
        }
        gm.plot();
    }
}
