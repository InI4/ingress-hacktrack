package de.spieleck.ingress.hackstat;

import java.util.Arrays;
import java.util.Iterator;
import java.awt.geom.Point2D;

/**
 * Optimizes a function in one variable by pute function evaluation.
 * Uses a interval length, function value heuristic, which is proven to be convergent.
 */
public class GOpt1D
    implements Iterable<Point2D.Double>
{
    /** Some golden section magic for dividing intervals. */
    public final double DIVI = 1.0 - 1.0 / (0.5 + Math.sqrt(1.25));

    /** 
     * A very simple forward linked list, to make this running in O(n) complexity,
     * where n is the number of function evaluations.
     */
    private class Node extends Point2D.Double
    {
        private Node next;
        public void setNext(Node next) { this.next = next; }
        public Node getNext() { return next; }
        public Node(Node next, double x, double y)
        {
            super(x,y);
            setNext(next);
        }
    }

    /**
     * Spec for the function to be optimized.
     */ 
    public interface Function
    {
        public double eval(double x);
    }

    /** Intervall bounds for optimization. */
    private double a, b;

    /** 
     * Creates an optimizer for some intervall [a,b].
     */ 
    public GOpt1D(double a, double b)
    {
        this.a = a;
        this.b = b;
    }

    /**
     * Function evaluations of the last optimization run. 
     * Kept to be iterable.
     */
    private Node head = null;

    /** Iterable interface. */
    @Override
    public Iterator<Point2D.Double> iterator() { return new Iterator<Point2D.Double>() {
            private Node pos = head;
            public boolean hasNext() { return pos != null && pos.getNext() != null; }
            public Point2D.Double next() { pos = pos.getNext(); return pos; }
            public void remove() { throw new UnsupportedOperationException("Cannot remove!"); }
        }; }

    /**
     * Optimize the given function with a specified (relative) accuracy.
     */
    public double opt(Function f, double eps)
    {
        eps *= Math.abs(a-b); // make accuracy relative
        double x0 = Math.min(a,b);
        head = new Node(null, x0, f.eval(x0));
        x0 = Math.max(a,b);
        Node nn = new Node(null, x0, f.eval(x0));
        head.setNext(nn);
        double xMax, yMax;
        if ( head.getY() > nn.getY() ) nn = head;
        xMax = head.getX();
        yMax = head.getY();
        double dxMax;
        do {
            dxMax = 0.0;
            double optValue = Double.MAX_VALUE;
            Node n0 = head, n1;
            while ( (n1 = n0.getNext()) != null ) {
                double dx = n1.getX() - n0.getX();
                dxMax = Math.max(dxMax, dx);
                double dy = 2.0*yMax - n1.getY() - n0.getY();
                double value = (eps + dy)/Math.pow(dx,1.5);
                if ( value < optValue ) {
                    optValue = value;
                    nn = n0;
                }
                n0 = n1;
            }
            Node n2 = nn.getNext();
            double h = n2.getY() - nn.getY();
            double ratio = h > 0.0 ? DIVI : h < 0.0 ? 1.0 - DIVI : 0.5;
            x0 = nn.getX() + ratio*(n2.getX() - nn.getX());
            double y0 = f.eval(x0);
            Node n3 = new Node(n2, x0, y0);
            nn.setNext(n3);
            if ( y0 > yMax ) {
                xMax = x0;
                yMax = y0;
            }
        } while ( dxMax > eps );
        return xMax;
    } 
}
