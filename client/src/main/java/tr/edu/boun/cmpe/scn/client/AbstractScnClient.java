package tr.edu.boun.cmpe.scn.client;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

/**
 * Created by esinka on 2/4/2017.
 */
public abstract class AbstractScnClient {

    abstract DatagramSocket getSocket();

    private Object mutex = new Object();

    SimpleDateFormat format;
    static final String DATE_PATTERN = "yyyy-mm-dd HH:mm:ss.SSS";

    ServiceData sendAndReceive(ServiceInterest interest) throws IOException {
        Gson gson = new Gson();
        String payload = gson.toJson(interest);
        byte[] data = payload.getBytes(Constants.UTF8);

        InetAddress address = InetAddress.getByName(Constants.SCN_BROADCAST_ADDRESS);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, Constants.SCN_SERVICE_PORT);

        //SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(srcAddress), srcPort);
        //datagramSocket = new DatagramSocket(socketAddress);
        DatagramSocket datagramSocket = getSocket();

        System.out.println("Sending ServiceInterest via " + datagramSocket.getLocalSocketAddress() + " Payload=" + payload);
        datagramSocket.send(packet);

        // Set a receive timeout, 10000 milliseconds
        datagramSocket.setSoTimeout(10000);
        System.out.println("Datagram socket receive buffer size:" + datagramSocket.getReceiveBufferSize());
        // Prepare the packet for receive
        packet.setData(new byte[Constants.PACKET_SIZE]);
        // Wait for a response from the server
        datagramSocket.receive(packet);
        String received = new String(packet.getData(), Constants.UTF8);

        System.out.println(formatTime() + " ServiceData received via " + datagramSocket.getLocalSocketAddress() + " Payload=" + received.trim());

        return gson.fromJson(received.trim(), ServiceData.class);
    }

    void send(ServiceInterest interest) throws IOException {
        Gson gson = new Gson();
        String payload = gson.toJson(interest);
        byte[] data = payload.getBytes(Constants.UTF8);

        InetAddress address = InetAddress.getByName(Constants.SCN_BROADCAST_ADDRESS);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, Constants.SCN_SERVICE_PORT);

        DatagramSocket datagramSocket = getSocket();

        System.out.println(formatTime() + " Sending ServiceInterest via " + datagramSocket.getLocalSocketAddress() + " Payload=" + payload);
        synchronized (mutex) {
            datagramSocket.send(packet);
        }
    }

    String formatTime() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.format(System.currentTimeMillis());
    }
}
