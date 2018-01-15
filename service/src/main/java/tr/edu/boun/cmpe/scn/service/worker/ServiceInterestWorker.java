package tr.edu.boun.cmpe.scn.service.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.net.DatagramPacket;

/**
 * Created by esinka on 1/28/2017.
 */
public class ServiceInterestWorker extends BaseWorker implements Runnable {

    private static final Logger log = LogManager.getLogger(ScnServer.class.getName());

    private ServiceInterest message;
    private DatagramPacket datagramPacket;
    private String srcIpAddress;

    public ServiceInterestWorker(ScnServer server, ServiceInterest message, DatagramPacket datagramPacket, String srcIpAddress) {

        super(server);
        this.message = message;
        this.datagramPacket = datagramPacket;
        this.srcIpAddress = srcIpAddress;
    }

    @Override
    public void run() {
        log.info("{}", Tool.logForService(srcIpAddress, datagramPacket.getAddress().toString()));
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