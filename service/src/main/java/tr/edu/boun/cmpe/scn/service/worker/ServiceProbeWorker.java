package tr.edu.boun.cmpe.scn.service.worker;

import tr.edu.boun.cmpe.scn.api.message.ServiceProbe;
import tr.edu.boun.cmpe.scn.service.CpuReader;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.io.IOException;
import java.net.DatagramPacket;

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
            String cpuTime = CpuReader.getInstance().getCpuTime();
            if (cpuTime != null) {
                message.setCpuUsage(cpuTime);
                //reply the probe back
                reply(message, datagramPacket);
            }
        } catch (IOException e) {
            System.err.println("Unable to read CPU usage!");
            e.printStackTrace();
        }

    }
}
