package tr.edu.boun.cmpe.scn.service;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.api.message.ServiceProbe;
import tr.edu.boun.cmpe.scn.api.message.ServiceUp;
import tr.edu.boun.cmpe.scn.service.worker.ServiceInterestWorker;
import tr.edu.boun.cmpe.scn.service.worker.ServiceProbeWorker;
import tr.edu.boun.cmpe.scn.service.worker.ServiceUpWorker;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by esinka on 1/28/2017.
 */
public class ScnServer implements Runnable {

    public static final int SERVICE_PORT = 9595;
    public static final String SERVICE_NAME = "s1";
    public static final String UTF8 = "UTF-8";
    public static final String BROADCAST_IP = "255.255.255.255";
    //creating a pool of 20 threads
    private ExecutorService executor = Executors.newFixedThreadPool(20);

    private boolean keepGoing = true;

    private DatagramSocket socket;

    public ScnServer() throws SocketException {
        socket = new DatagramSocket(SERVICE_PORT);
    }

    @Override
    public void run() {
        System.out.println(SERVICE_NAME + " service started and listening to port " + SERVICE_PORT);
        //emit service up first
        executor.submit(new ServiceUpWorker(this));

        while (keepGoing) {
            try {
                DatagramPacket received = receive();
                getAndProcessData(received);
            } catch (IOException|RuntimeException e) {
                System.err.println("Can not receive packet due to the exception:" + e.toString());
                e.printStackTrace();
                continue;
            }
        }

        System.out.println(SERVICE_NAME + " stopped");
    }

    private DatagramPacket receive() throws IOException {
        byte[] receiveData = new byte[4096];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        return receivePacket;
    }

    private ScnMessage getAndProcessData(DatagramPacket packet) throws UnsupportedEncodingException {
        String data = new String(packet.getData(), UTF8).trim();
        Gson gson = gson();
        ScnMessage scnMessage = gson.fromJson(data.trim(), ScnMessage.class);

        ScnMessageType scnMessageType = ScnMessageType.valueOf(scnMessage.getMessageTypeId());
        switch (scnMessageType) {
            case INTEREST:
                ServiceInterest interest = gson.fromJson(data, ServiceInterest.class);
                System.out.println("ServiceInterest received from " + packet.getAddress() + ":" + packet.getPort() + " Data:" + gson.toJson(interest));
                executor.submit(new ServiceInterestWorker(this, interest, packet));
                break;
            case PROBE:
                ServiceProbe probe = gson().fromJson(data, ServiceProbe.class);
                System.out.println("ServiceProbe received:" + packet.getAddress() + ":" + packet.getPort() + " Data:" + gson.toJson(probe));
                executor.submit(new ServiceProbeWorker(this, probe, packet));
                break;
            default:
                System.out.println("Message dropped: " + data);
        }
        return scnMessage;
    }

    public void reply(ScnMessage reply, DatagramPacket packetReceived) {
        String message = gson().toJson(reply);
        send(reply, packetReceived.getAddress(), packetReceived.getPort());
    }

    public void send(ScnMessage scnMessage, InetAddress address, int port) {
        String message = gson().toJson(scnMessage);
        DatagramPacket dp = new DatagramPacket(message.getBytes(), message.getBytes().length, address, port);
        try {
            socket.send(dp);
            System.out.println("Sent to " + address + ":" + port + " Message:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Gson gson() {
        return new Gson();
    }


}

