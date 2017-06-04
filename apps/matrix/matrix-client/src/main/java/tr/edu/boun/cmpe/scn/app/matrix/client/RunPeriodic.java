package tr.edu.boun.cmpe.scn.app.matrix.client;

import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.Argument;
import tr.edu.boun.cmpe.scn.api.message.Arguments;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.app.matrix.common.Constants;
import tr.edu.boun.cmpe.scn.client.IScnClient;
import tr.edu.boun.cmpe.scn.client.ScnClient;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by esinka on 5/26/2017.
 */
public class RunPeriodic {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: <Matrix dimension> <IP Address> <Period in milliseconds> <Number of requests (optional)>");
            return;
        }
        String dimension = args[0];
        if (dimension == null || !Util.isNumeric(dimension)) {
            System.out.println("Matrix dimension must be given");
            return;
        }
        String address = args[1];
        if (address == null) {
            System.out.println("IP address must be given");
            return;
        }

        String period = args[2];
        if (period == null || !Util.isNumeric(period)) {
            System.out.println("Period must be given");
            return;
        }

        Integer numOfReq = Integer.MAX_VALUE;
        String requestCount = args[3];
        if (requestCount != null && Util.isNumeric(requestCount)) {
            numOfReq = Integer.parseInt(requestCount);
        }

        System.out.println("Request count=" + numOfReq);

        long sleepTime = Long.parseLong(period);
        int index = 0;
        try {
            IScnClient client = ScnClient.newInstance(Constants.SERVICE_NAME, address);
            while (index < numOfReq) {
                client.send(buildInterest(dimension), new ScnListener());
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("Quitting the loop!!");
                }
                index++;
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ServiceInterest buildInterest(String dimension) {
        ServiceInterest si = new ServiceInterest();
        si.setServiceName(Constants.SERVICE_NAME);
        si.setMessageId(String.valueOf(System.currentTimeMillis()));
        si.setMessageTypeId(ScnMessageType.INTEREST.getId());
        Arguments args = new Arguments();
        Argument arg = new Argument();
        arg.setName(Constants.ARG_KEY);
        arg.setValue(dimension);
        args.getArgument().add(arg);
        si.setArguments(args);
        return si;
    }
}
