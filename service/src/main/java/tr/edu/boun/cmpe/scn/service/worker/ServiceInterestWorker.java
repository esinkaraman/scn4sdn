package tr.edu.boun.cmpe.scn.service.worker;

import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.net.DatagramPacket;

/**
 * Created by esinka on 1/28/2017.
 */
public class ServiceInterestWorker extends BaseWorker implements Runnable {

    private ServiceInterest message;
    private DatagramPacket datagramPacket;

    public ServiceInterestWorker(ScnServer server, ServiceInterest message, DatagramPacket datagramPacket) {
        super(server);
        this.message = message;
        this.datagramPacket = datagramPacket;
    }

    @Override
    public void run() {
        try {
            ServiceData serviceData = server.getInterestListener().processInterest(message);
            if (serviceData != null) {
                reply(serviceData, datagramPacket);
            }
        } catch (Exception e) {
            System.err.println("Unable to process service interest");
            e.printStackTrace();
        }
    }
}