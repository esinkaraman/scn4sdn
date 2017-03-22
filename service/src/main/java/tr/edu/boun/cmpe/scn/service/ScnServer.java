package tr.edu.boun.cmpe.scn.service;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.api.message.ServiceProbe;
import tr.edu.boun.cmpe.scn.service.worker.ServiceInterestWorker;
import tr.edu.boun.cmpe.scn.service.worker.ServiceProbeWorker;
import tr.edu.boun.cmpe.scn.service.worker.ServiceUpWorker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by esinka on 1/28/2017.
 */
public class ScnServer implements Runnable {

    private String serviceName;
    private int servicePort;
    private String cpuResource;
    //creating a pool of 50 threads
    private ExecutorService executor = Executors.newFixedThreadPool(50);

    private boolean keepGoing = true;

    private DatagramSocket socket;

    public ScnServer(String srcIp, int servicePort, String serviceName, String cpuResource) throws SocketException, UnknownHostException {
        Tool.checkNull(srcIp, "Source IP address can not be null");
        Tool.checkNull(serviceName, "Service name can not be null");
        Tool.checkNull(cpuResource, "CpuResource can not be null");
        SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(srcIp), servicePort);
        socket = new DatagramSocket(socketAddress);
        this.servicePort = servicePort;
        this.serviceName = serviceName;
        this.cpuResource = cpuResource;
    }

    @Override
    public void run() {
        System.out.println(serviceName + " service started and listening to port " + servicePort);
        //emit service up first
        executor.submit(new ServiceUpWorker(this));

        while (keepGoing) {
            try {
                DatagramPacket received = receive();
                getAndProcessData(received);
            } catch (IOException | RuntimeException e) {
                System.err.println("Can not receive packet due to the exception:" + e.toString());
                e.printStackTrace();
                continue;
            }
        }

        System.out.println(serviceName + " stopped");
    }

    private DatagramPacket receive() throws IOException {
        byte[] receiveData = new byte[Constants.PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        return receivePacket;
    }

    private ScnMessage getAndProcessData(DatagramPacket packet) throws UnsupportedEncodingException {
        String data = new String(packet.getData(), Constants.UTF8).trim();
        Gson gson = gson();
        ScnMessage scnMessage = gson.fromJson(data.trim(), ScnMessage.class);

        ScnMessageType scnMessageType = ScnMessageType.valueOf(scnMessage.getMessageTypeId());
        switch (scnMessageType) {
            case INTEREST:
                ServiceInterest interest = gson.fromJson(data, ServiceInterest.class);
                System.out.println("SERVICE INTEREST received from " + packet.getAddress() + ":" + packet.getPort() + " Data:" + gson.toJson(interest));
                executor.submit(new ServiceInterestWorker(this, interest, packet));
                break;
            case PROBE:
                ServiceProbe probe = gson().fromJson(data, ServiceProbe.class);
                System.out.println("PROBE received from " + packet.getAddress() + ":" + packet.getPort() + " Data:" + gson.toJson(probe));
                executor.submit(new ServiceProbeWorker(this, probe, packet));
                break;
            default:
                System.out.println("Message dropped: " + data);
        }
        return scnMessage;
    }

    public void reply(ScnMessage reply, DatagramPacket packetReceived) {
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

    public String getServiceName() {
        return serviceName;
    }

    public int getServicePort() {
        return servicePort;
    }

    public String getCpuResource() {
        return cpuResource;
    }
}

