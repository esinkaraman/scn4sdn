package tr.edu.boun.cmpe.scn.service.worker;

import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
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
        //reply response
        ServiceData serviceData = prepareResponse(message);
        reply(serviceData, datagramPacket);
    }

    private ServiceData prepareResponse(ServiceInterest interest) {
        ServiceData serviceData = new ServiceData();
        serviceData.setServiceName(interest.getServiceName());
        serviceData.setMessageTypeId(ScnMessageType.DATA.getId());
        serviceData.setMessageId(interest.getMessageId());
        return serviceData;
    }

}
