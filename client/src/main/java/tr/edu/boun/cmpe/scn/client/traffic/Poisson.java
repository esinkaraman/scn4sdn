package tr.edu.boun.cmpe.scn.client.traffic;

/**
 * Created by esinka on 2/26/2017.
 */
public class Poisson {

    public static double sample(int mean) {
        double lambda = 1.0 / mean;
        return (Math.log(1.0 - Math.random()) / lambda) * -1;
    }
}
