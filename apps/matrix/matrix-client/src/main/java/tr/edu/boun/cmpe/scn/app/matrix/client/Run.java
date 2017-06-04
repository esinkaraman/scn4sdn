package tr.edu.boun.cmpe.scn.app.matrix.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by esinka on 3/22/2017.
 */
public class Run {

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: <Poisson mean> " +
                                       "<Duration time (seconds)> " +
                                       "<Matrix dimension range start> " +
                                       "<Matrix dimension range end> " +
                                       "<IP Address> " +
                                       "<Waiting time before shut down (seconds)>");
            return;
        }
        String mean = args[0];
        if (mean == null || !Util.isNumeric(mean)) {
            System.out.println("Poisson mean must be given");
            return;
        }
        String durationSeconds = args[1];
        if (durationSeconds == null || !Util.isNumeric(durationSeconds)) {
            System.out.println("Duration time must be given");
            return;
        }
        String dimensionStart = args[2];
        if (dimensionStart == null || !Util.isNumeric(dimensionStart)) {
            System.out.println("<Matrix dimension range start must be given");
            return;
        }

        String dimensionEnd = args[3];
        if (dimensionEnd == null || !Util.isNumeric(dimensionEnd)) {
            System.out.println("<Matrix dimension range end must be given");
            return;
        }

        String address = args[4];
        if (address == null) {
            System.out.println("IP address must be given");
            return;
        }
        String waitForShutdown = args[5];
        if (waitForShutdown == null || !Util.isNumeric(waitForShutdown)) {
            System.out.println("Waiting time before shut down must be given");
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            RequestGenerator generator = new RequestGenerator(Integer.parseInt(mean),
                                                              Integer.parseInt(durationSeconds),
                                                              Integer.parseInt(dimensionStart),
                                                              Integer.parseInt(dimensionEnd),
                                                              address,
                                                              Integer.parseInt(waitForShutdown));
            executorService.execute(generator);
        } finally {
            executorService.shutdown();
        }
    }
}