package de.spieleck.ingress.hackstat;

public class NumberDiscreteDistr<T extends Number>
    extends DiscreteDistr<T>
{
    // XXX use TObjectIntIterators?!
    public double getAverage()
    {
        double sum = 0.0;
        for(T key : keys()) {
            sum += ((Number)key).doubleValue() * getRaw(key);
        }
        return sum / getTotal();
    }

    public double getSdev()
    {
        double xm = getAverage();
        double sum = 0.0;
        for(T key : keys()) {
            double h = ((Number)key).doubleValue()-xm;
            sum += h * h * getRaw(key);
        }
        return sum / (getTotal() - 1);
    }

}
