package tr.edu.boun.cmpe.scn.service.worker;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.message.ServiceProbe;
import tr.edu.boun.cmpe.scn.service.CpuReader;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by esinka on 1/28/2017.
 */
public class ServiceProbeWorker extends BaseWorker implements Runnable {

    private ServiceProbe message;
    private DatagramPacket datagramPacket;

    public ServiceProbeWorker(ScnServer server, ServiceProbe message, DatagramPacket datagramPacket) {
        super(server);
        this.message = message;
        this.datagramPacket = datagramPacket;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            message.setCpuUsage(CpuReader.getCpuTime(server.getCpuResource()));
            //reply the probe back
            Gson gson = new Gson();
            String payload = gson.toJson(message);
            byte[] data = payload.getBytes(Constants.UTF8);

            InetAddress destAddress = InetAddress.getByName(Constants.SCN_BROADCAST_ADDRESS);
            int destPort = Constants.SCN_SERVICE_PORT;
            DatagramPacket packet = new DatagramPacket(data, data.length, destAddress, destPort);

            reply(message, packet);
        } catch (IOException e) {
            System.err.println("Unable to read CPU usage!");
            e.printStackTrace();
        }

    }
}
