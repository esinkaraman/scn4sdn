package tr.edu.boun.cmpe.scn.service.worker;

import tr.edu.boun.cmpe.scn.api.message.ScnMessage;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by esinka on 1/28/2017.
 */
public abstract class BaseWorker {

    private ScnServer server;

    BaseWorker(ScnServer server) {
        this.server = server;
    }

    void reply(ScnMessage message, DatagramPacket received) {
        server.reply(message, received);
    }

    void send(ScnMessage scnMessage, InetAddress address, int port) {
        server.send(scnMessage, address, port);
    }

}