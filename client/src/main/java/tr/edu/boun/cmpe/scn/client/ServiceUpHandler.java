package tr.edu.boun.cmpe.scn.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by esinka on 1/6/2017.
 */
public class ServiceUpHandler {
    public static void main(String[] args) {
        DatagramSocket datagramSocket = null;
        try {
            //String srcIp = InetAddress.getLocalHost().getHostAddress();
            if(args.length < 3) {
                System.out.println("Usage: (SourceIp)(ServiceName)(ServicePort)");
                return;
            }
            String srcIp = args[0];
            if(srcIp == null) {
                System.out.println("Source IP must bu given!");
                return;
            }
            String serviceName = args[1];
            if(serviceName == null) {
                System.out.println("ServiceName must bu given!");
                return;
            }
            String servicePort = args[2];
            if(serviceName == null) {
                System.out.println("ServicePort must bu given!");
                return;
            }

            String dstIp = "255.255.255.255";
            int srcPort = 1742;
            int dstPort = 1743;
            String payload = new StringBuilder().append(serviceName).append(":").append(servicePort).toString();
            byte[] data = payload.getBytes("UTF-8");
            InetAddress address = InetAddress.getByName(dstIp);
            DatagramPacket packet = new DatagramPacket(
                    data, data.length, address, dstPort
            );
            SocketAddress a = new InetSocketAddress(InetAddress.getByName(srcIp), srcPort);
            datagramSocket = new DatagramSocket(a);
            datagramSocket.send(packet);
            System.out.println("udp sent successfully. SrcIp=" + srcIp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

    }
}
