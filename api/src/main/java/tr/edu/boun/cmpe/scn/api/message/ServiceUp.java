package tr.edu.boun.cmpe.scn.api.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tr.edu.boun.cmpe.scn.api.common.Constants;

import java.io.UnsupportedEncodingException;

public class ServiceUp extends ScnMessage {

    protected int servicePort;

    /**
     * Gets the value of the servicePort property.
     */
    public int getServicePort() {
        return servicePort;
    }

    /**
     * Sets the value of the servicePort property.
     */
    public void setServicePort(int value) {
        this.servicePort = value;
    }

    @Override
    public byte[] serialize() {
        byte[] payload = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            payload = mapper.writeValueAsString(copy()).getBytes(Constants.UTF8);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            System.err.println("Unable to serialize the packet " + this.getClass());
            e.printStackTrace();
        }
        return payload == null ? new byte[0] : payload;
    }

    private ServiceUp copy() {
        ServiceUp up = new ServiceUp();
        up.setServicePort(servicePort);
        up.setMessageTypeId(messageTypeId);
        up.setServiceName(serviceName);
        return up;
    }

}
