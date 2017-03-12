package tr.edu.boun.cmpe.scn.client;

import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.Argument;
import tr.edu.boun.cmpe.scn.api.message.Arguments;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

import java.io.IOException;

/**
 * Created by esinka on 2/4/2017.
 */
public class TestInterest {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: <IP Address>");
            return;
        }
        if (args[0] == null) {
            System.out.println("IP address must be given.");
            return;
        }
        String address = args[0];
        String serviceName = "s1";
        ServiceInterest interest = new ServiceInterest();
        interest.setMessageTypeId(ScnMessageType.INTEREST.getId());
        interest.setServiceName(serviceName);
        interest.setMessageId("1");

        Arguments arguments = new Arguments();
        interest.setArguments(arguments);
        Argument arg = new Argument();
        arg.setName("a1");
        arg.setValue("avalue1");
        interest.getArguments().getArgument().add(arg);
        IScnClient client = null;

        try {
            client = ScnClient.newInstance(serviceName, address);
            ServiceData serviceData = client.sendAndReceive(interest);
            System.out.println("received " + serviceData);
        } catch (IOException e) {
            System.out.println("exception occurred!!!");
            e.printStackTrace();
        } finally {
            if(client != null) {
                client.close();
            }
        }

    }
}
