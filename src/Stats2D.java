package de.spieleck.ingress.hackstat;

public class Stats2D
{
	private int n;
	public double[] x, y;

	public Stats2D()
	{
		this(100);
	}

	public Stats2D(int size)
	{
		x = new double[size];
		y = new double[size];
		reset();
	}

	public void reset()
	{
		n = 0;
	}

	private void checkSpace()
	{
		if ( n+1 == x.length ) 
		{
			double[] h;
			h = new double[x.length * 2];
			System.arraycopy(x, 0, h, 0, n);
			x = h;
			h = new double[y.length * 2];
			System.arraycopy(y, 0, h, 0, n);
			y = h;
		}
	}

	public int add(double x, double y)
	{
		checkSpace();
		this.x[n] = x;
		this.y[n] = y;
		return ++n;
	}

	public int getN()
	{
		return n;
	}

	public double getSumX() { return sum(x); }
	public double getAvgX() { return sum(x) / n; }
	public double getSumY() { return sum(y); }
	public double getAvgY() { return sum(y) / n; }

	private double sum(double[] t)
	{
		double sum = 0.0;
		for(int i = 0; i < n; i++) sum += t[i];
		return sum;
	}

	public double correlation()
	{
		double xm = getAvgX();
		double ym = getAvgY();
		double sxy = 0.0, sx2 = 0.0, sy2 = 0.0;
		for(int i = 0; i < n; i++) {
			double dx = x[i] - xm, dy = y[i] - ym;
			sxy += dx * dy;
			sx2 += dx*dx;
			sy2 += dy*dy;
		}
		return sxy/Math.sqrt(sx2*sy2);
	}

	public static void main(String[] args)
	{
		Stats2D test = new Stats2D();
		test.add(1,1.0);
		test.add(2, -1.0);
		test.add(3, -3.0);
		System.out.println("test1="+test.correlation());
		test.add(4, -1.0);
		System.out.println("test2="+test.correlation());
		test.add(3, 2.0);
		System.out.println("test2="+test.correlation());
		test.add(2, 0.0);
		System.out.println("test2="+test.correlation());
		test.add(1, -3.0);
		System.out.println("test2="+test.correlation());
		test.add(5, 5.0);
		System.out.println("test2="+test.correlation());
	}

}
