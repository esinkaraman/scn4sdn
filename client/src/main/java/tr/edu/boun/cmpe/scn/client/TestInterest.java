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

    IScnClient client = null;

    public static void main(String[] args) {
        new TestInterest().sendInterest(args);
    }

    public void sendInterest(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: <IP Address> <Service Name>");
            return;
        }
        if (args[0] == null) {
            System.out.println("IP address must be given.");
            return;
        }
        if (args[1] == null) {
            System.out.println("Service name must be given.");
            return;
        }
        String address = args[0];
        String serviceName = args[1];
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

        ResponseListener listener = new ResponseListener();

        try {
            client = ScnClient.newInstance(serviceName, address);
            client.send(interest, listener);
        } catch (IOException e) {
            System.out.println("exception occurred!!!");
            e.printStackTrace();
        }
    }

    public class ResponseListener implements IScnListener {

        @Override
        public void received(ServiceData serviceData, String ipAddressClient) {
            System.out.println("ServiceData received:" + serviceData);

            if (client != null) {
                client.close();
            }
        }
    }
}
