package tr.edu.boun.cmpe.scn.client;

import com.google.gson.Gson;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tr.edu.boun.cmpe.scn.api.common.Tool.checkArgument;
import static tr.edu.boun.cmpe.scn.api.common.Tool.checkNull;

/**
 * Created by esinka on 2/4/2017.
 */
public class ScnClient extends AbstractScnClient implements IScnClient {

    private static ConcurrentHashMap<String, ScnClient> serviceNameToInstanceMap = new ConcurrentHashMap<>();
    private String serviceName;
    private DatagramSocket socket;
    private ConcurrentHashMap<String, IScnListener> messageIdToListener;
    private ScnListener scnListener;
    private ExecutorService executorService;

    private Gson gson;

    private static final Object mutex = new Object();

    private ScnClient(String serviceName, String srcIpAddress) throws UnknownHostException, SocketException {
        this.serviceName = serviceName;
        int srcPort = CyclicCounter.getInstance().cyclicallyIncrementAndGet();
        SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(srcIpAddress), srcPort);
        socket = new DatagramSocket(socketAddress);
        messageIdToListener = new ConcurrentHashMap<>();

        scnListener = new ScnListener(this);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(scnListener);

        gson = new Gson();
    }

    public static ScnClient newInstance(String serviceName, String srcIpAddress) throws SocketException, UnknownHostException {
        checkNull(serviceName, "Service name can not be null");
        checkNull(srcIpAddress, "Source IP address name can not be null");
        ScnClient scnClient = serviceNameToInstanceMap.get(serviceName);
        if (scnClient == null) {
            synchronized (mutex) {
                if (scnClient == null) {
                    scnClient = new ScnClient(serviceName, srcIpAddress);
                    serviceNameToInstanceMap.put(serviceName, scnClient);
                }
            }
        }
        return scnClient;
    }

    public static ScnClient getInstance(String serviceName) {
        checkNull(serviceName, "Service name can not be null");
        ScnClient scnClient = serviceNameToInstanceMap.get(serviceName);
        checkArgument(scnClient != null, "There is no instance of " + serviceName);
        return scnClient;
    }

    @Override
    public ServiceData sendSynchronous(ServiceInterest interest) throws IOException {
        checkParameters(interest);
        return sendAndReceive(interest);
    }

    @Override
    public void send(ServiceInterest interest, IScnListener listener) throws IOException {
        checkParameters(interest);
        send(interest);
        messageIdToListener.put(interest.getMessageId(), listener);
    }

    private void checkParameters(ServiceInterest interest) {
        checkNull(interest, "ServiceInterest can not be null");
        checkNull(interest.getServiceName(), "ServiceName can not be null");
        checkArgument(serviceName.equals(interest.getServiceName()), "ServiceName must be " + serviceName);
        checkNull(interest.getMessageId(), "MessageId can not be null");
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        scnListener.stop();
        executorService.shutdown();
    }

    @Override
    DatagramSocket getSocket() {
        return socket;
    }

    String getServiceName() {
        return serviceName;
    }

    protected void received(ScnMessage scnMessage, String payload) {
        ScnMessageType scnMessageType = ScnMessageType.valueOf(scnMessage.getMessageTypeId());

        if (scnMessageType == null) {
            System.out.println("Invalid SCN message type! " + payload);
            return;
        }

        if (scnMessageType.equals(ScnMessageType.DATA)) {
            serviceDataReceived(payload);
        }
    }

    private void serviceDataReceived(String payload) {
        ServiceData serviceData = gson.fromJson(payload, ServiceData.class);
        IScnListener iScnListener = messageIdToListener.get(serviceData.getMessageId());
        if (iScnListener == null) {
            System.out.println("No listener found for ServiceData!!! " + payload);
            return;
        }
        iScnListener.received(serviceData);
    }

}
