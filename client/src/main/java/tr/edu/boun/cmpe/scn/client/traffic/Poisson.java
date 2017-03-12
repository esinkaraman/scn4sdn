package tr.edu.boun.cmpe.scn.client.traffic;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by esinka on 2/26/2017.
 */
public class Poisson {
    static final double e = 1.0E-12D;
    DecimalFormat df = new DecimalFormat("#.###");

    public void poisson() {
        PoissonDistribution poissonDistribution = new PoissonDistribution(7);
        double cumulative = 0;
        int[] sample = poissonDistribution.sample(20);
        System.out.println(Arrays.toString(sample));
        for (int i = 0; i < 21; i++) {
            double probability = poissonDistribution.probability(i);
            System.out.println(i + " " + probability + " " + round(probability,10) + " " + df.format(probability) + " " + sample[i]);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double poisson(int k, double lambda) {
        double b = Math.pow(lambda, k) * Math.pow(e, -1 * lambda);
        return b / factorial(k);
    }

    public static void main(String[] args) {
        Poisson p = new Poisson();
        p.poisson();
        int k = 0;
        /*while(k<=10) {
            double poisson = p.poisson(k, 1.6);
            System.out.printf("\n%d\t %f",k,poisson);
            k++;
        }*/

    }

    public int factorial(int n) {
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result = result * i;
        }
        return result;
    }
}
