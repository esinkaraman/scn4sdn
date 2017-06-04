package tr.edu.boun.cmpe.scn.app.matrix.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.Argument;
import tr.edu.boun.cmpe.scn.api.message.Arguments;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.app.matrix.common.Constants;
import tr.edu.boun.cmpe.scn.client.ScnClient;
import tr.edu.boun.cmpe.scn.client.traffic.Poisson;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by esinka on 3/22/2017.
 */
public class RequestGenerator implements Runnable {
    private static final Logger log = LogManager.getLogger(RequestGenerator.class.getName());

    public final Random random = new Random();
    int poissonMean;
    long runTimeInMilliseconds;
    int dimensionStart;
    int dimensionEnd;
    String address;
    int waitBeforeShutdown;
    long startTime;

    public RequestGenerator(int poissonMean, int runTimeInSeconds, int dimensionStart, int dimensionEnd, String address, int waitBeforeShutdown) {
        this.poissonMean = poissonMean;
        this.runTimeInMilliseconds = runTimeInSeconds * 1000;
        this.dimensionStart = dimensionStart;
        this.dimensionEnd = dimensionEnd;
        this.address = address;
        this.waitBeforeShutdown = waitBeforeShutdown;
        this.startTime = System.currentTimeMillis();
    }

    boolean timeRunOut(long sleepTime) {
        return runTimeInMilliseconds <= elapsed(sleepTime);
    }

    long elapsed(long sleepTime) {
        return (System.currentTimeMillis() + sleepTime) - startTime;
    }

    int getRandomlyFromRange(int rangeStart, int rangeEnd) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd + 1);
    }

    @Override
    public void run() {
        ScnClient scnClient = null;
        try {
            scnClient = ScnClient.newInstance(Constants.SERVICE_NAME, address);
            boolean cont = true;
            int reqCount = 0;
            while (cont) {
                try {
                    long sleepTime = (long) Poisson.sample(poissonMean);

                    if (timeRunOut(sleepTime)) {
                        System.out.println("The time runs out. Ending the client with total request count " + reqCount);
                        cont = false;
                        continue;
                    }

                    System.out.println("Matrix:Sleeping for " + sleepTime + " seconds.");
                    Thread.sleep(sleepTime);

                    scnClient.send(buildInterest(), new ScnListener());
                    reqCount++;

                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                System.out.println("Waiting before shut down for milliseconds of " + waitBeforeShutdown);
                Thread.sleep(waitBeforeShutdown);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Closing...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            if (scnClient != null) {
                scnClient.close();
            }
        }
    }

    private ServiceInterest buildInterest() {
        ServiceInterest si = new ServiceInterest();
        si.setServiceName(Constants.SERVICE_NAME);
        si.setMessageId(generateMsgId());
        si.setMessageTypeId(ScnMessageType.INTEREST.getId());
        Arguments args = new Arguments();
        Argument arg = new Argument();
        arg.setName(Constants.ARG_KEY);
        arg.setValue(String.valueOf(getRandomlyFromRange(dimensionStart, dimensionEnd)));
        args.getArgument().add(arg);
        si.setArguments(args);
        return si;
    }

    private String generateMsgId() {
        //int randomInt = Tool.generateRandomInteger(10000, 99999, random);
        int randomInt = Tool.getRandomlyFromRange(10000, 99999);
        return new StringBuilder().append(System.currentTimeMillis()).append(randomInt).toString();
    }
}
