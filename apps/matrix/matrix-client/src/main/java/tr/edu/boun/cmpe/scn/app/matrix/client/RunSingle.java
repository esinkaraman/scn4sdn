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
 * Created by esinka on 4/4/2017.
 */
public class RunSingle {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: <Matrix dimension> <IP Address>");
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

        try {
            IScnClient client = ScnClient.newInstance(Constants.SERVICE_NAME,address);
            client.send(buildInterest(dimension),  new ScnListener());

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
