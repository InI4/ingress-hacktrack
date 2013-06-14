package de.spieleck.ingress.hackstat;

public class Stats1D
{
	private int n;
	public double[] x;

	public Stats1D()
	{
		this(100);
	}

	public Stats1D(int size)
	{
		x = new double[size];
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
		}
	}

	public int add(double x)
	{
		checkSpace();
		this.x[n] = x;
		return ++n;
	}

	public int n()
	{
		return n;
	}

	public double average()
  {
      return sum() / n;
  }

	public double sum()
	{
		double sum = 0.0;
		for(int i = 0; i < n; i++) sum += x[i];
		return sum;
	}

	public double sdev()
	{
    double av = average();
		double sum = 0.0;
		for(int i = 0; i < n; i++) {
        double h = x[i] - av;
        sum += h * h;
    }
		return Math.sqrt(sum/(n-1));
	}

	public double sdevAvg()
  {
      return sdev()/Math.sqrt(n);
  }

	public double squareSum()
	{
		double sum = 0.0;
		for(int i = 0; i < n; i++) sum += x[i]*x[i];
		return sum;
	}

  public double get(int i)
  {
      if ( i < 0 || i >= n ) {
          throw new RuntimeException("Stats out of range "+i+" from "+n);
      }
      return x[i];
  }

	public static void main(String[] args)
	{
		Stats1D test = new Stats1D();
		test.add(1.0);
		test.add(-1.0);
		test.add(-3.0);
		System.out.println(test.n()+", xm="+test.average()+", xs="+test.sdevAvg());
		test.add(-1.0);
		test.add(5.0);
		System.out.println(test.n()+", xm="+test.average()+", xs="+test.sdevAvg());
		test.add(0.0);
		System.out.println(test.n()+", xm="+test.average()+", xs="+test.sdevAvg());
		test.add(0.2);
		System.out.println(test.n()+", xm="+test.average()+", xs="+test.sdevAvg());
	}

}
