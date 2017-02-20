package tr.edu.boun.cmpe.scn.service.worker;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.ServiceUp;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by esinka on 1/28/2017.
 */
public class ServiceUpWorker extends BaseWorker implements Runnable {

    public ServiceUpWorker(ScnServer server) {
        super(server);
    }

    @Override
    public void run() {
        emitServiceUp();
    }

    private void emitServiceUp() {
        try {
            ServiceUp serviceUp = prepareServiceUp();
            String payload = gson().toJson(serviceUp);
            byte[] data = payload.getBytes(Constants.UTF8);

            InetAddress destAddress = InetAddress.getByName(Constants.SCN_BROADCAST_ADDRESS);
            int destPort = Constants.SCN_SERVICE_PORT;
            DatagramPacket packet = new DatagramPacket(data, data.length, destAddress, destPort);

            reply(serviceUp, packet);

            System.out.println("ServiceUP emitted. " + payload);

        } catch (IOException e) {
            System.err.println("Error while sending ServiceUp message!");
            e.printStackTrace();
        }
    }

    private ServiceUp prepareServiceUp() {
        ServiceUp sup = new ServiceUp();
        sup.setMessageTypeId(ScnMessageType.UP.getId());
        sup.setServiceName(server.getServiceName());
        sup.setServicePort(server.getServicePort());
        return sup;
    }

    private Gson gson() {
        return new Gson();
    }
}
