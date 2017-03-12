package tr.edu.boun.cmpe.scn.client;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by esinka on 3/1/2017.
 */
public class ScnListener implements Runnable {

    private DatagramSocket socket;
    private ScnClient client;
    private boolean keepContinuing = true;

    ScnListener(ScnClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        DatagramSocket datagramSocket = client.getSocket();

        System.out.println("Listening to " + datagramSocket + " for " + client.getServiceName());

        Gson gson = new Gson();
        while (keepContinuing) {
            try {
                byte[] packetBuffer = new byte[Constants.PACKET_SIZE];
                DatagramPacket inboundBasePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
                // Wait for a response from the server
                datagramSocket.receive(inboundBasePacket);
                String received = new String(inboundBasePacket.getData(), Constants.UTF8);

                System.out.println("ServiceData received via " + datagramSocket.getLocalSocketAddress() + " Payload=" + received.trim());

                ScnMessage scnMessage = gson.fromJson(received.trim(), ScnMessage.class);
                client.received(scnMessage, received.trim());

            } catch (Exception e) {
                System.out.println("Exception during listening to " + datagramSocket);
                e.printStackTrace();
            }
        }
    }

    void stop() {
        keepContinuing = false;
    }
}
