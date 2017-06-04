package tr.edu.boun.cmpe.scn.app.matrix.client;

import tr.edu.boun.cmpe.scn.client.IScnClient;
import tr.edu.boun.cmpe.scn.client.ScnClient;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by esinka on 3/22/2017.
 */
public class Client {
    IScnClient scnClient = null;

    public Client(String serviceName, String address) throws SocketException, UnknownHostException {
        scnClient = ScnClient.newInstance(serviceName, address);
    }

    
}
