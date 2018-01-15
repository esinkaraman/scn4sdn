package tr.edu.boun.cmpe.scn.client;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by esinka on 3/1/2017.
 */
public class ScnListener implements Runnable {

    private DatagramSocket socket;
    private ScnClient client;
    private boolean keepContinuing = true;
    //private Gson gson = new Gson();
    String localSocketAddress = null;

    private ExecutorService executor = Executors.newFixedThreadPool(48);
    private ExecutorService executorForClient = Executors.newFixedThreadPool(48);

    ScnListener(ScnClient client) {
        this.client = client;
        localSocketAddress = client.getSocket().getLocalSocketAddress().toString();
    }

    @Override
    public void run() {
        DatagramSocket datagramSocket = client.getSocket();

        System.out.println("Listening to " + datagramSocket + " for " + client.getServiceName());

        try {
            DatagramPacket inboundBasePacket;
            while (keepContinuing) {
                try {
                    byte[] packetBuffer = new byte[Constants.PACKET_SIZE];
                    inboundBasePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
                    // Wait for a response from the server
                    datagramSocket.receive(inboundBasePacket);

                    executor.submit(new Dispatcher(inboundBasePacket.getData()));

                } catch (Exception e) {
                    System.out.println("Exception during listening to " + datagramSocket +
                            " e:" + e.toString() + " e:" + e.getLocalizedMessage() + " e:" + e.getMessage());
                    e.printStackTrace();
                    System.err.println(e);
                    keepContinuing = false;
                }
            }
        } finally {
            System.out.println("Exiting listener thread...");
        }

    }

    void stop() {
        keepContinuing = false;
        client.getSocket().close();
        executor.shutdown();
        executorForClient.shutdown();
    }

    private class Dispatcher implements Runnable {

        byte[] bytes;

        public Dispatcher(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                String received = new String(bytes, Constants.UTF8);

                System.out.println(Tool.formatTime() + " ServiceData received via " + localSocketAddress + " Payload=" + received.trim());

                ScnMessage scnMessage = new Gson().fromJson(received.trim(), ScnMessage.class);
                //client.received(scnMessage, received.trim());
                executorForClient.submit(new ClientDispatcher(scnMessage, received));

            } catch (Exception e) {
                System.out.println("Exception during receiving ScnMessage e:" + e.toString() + " e:" + e.getLocalizedMessage() + " e:" + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private class ClientDispatcher implements Runnable {
        ScnMessage scnMessage;
        String payload;

        public ClientDispatcher(ScnMessage scnMessage, String payload) {
            this.scnMessage = scnMessage;
            this.payload = payload;
        }

        @Override
        public void run() {
            client.received(scnMessage, payload.trim());
        }
    }

}
