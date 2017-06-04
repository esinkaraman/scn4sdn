package tr.edu.boun.cmpe.scn.api.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tr.edu.boun.cmpe.scn.api.common.Constants;

import java.io.UnsupportedEncodingException;


public class ServiceProbe extends ScnMessage {

    protected String cpuUsage;

    /**
     * Gets the value of the cpuUsage property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Sets the value of the cpuUsage property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCpuUsage(String value) {
        this.cpuUsage = value;
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

    private ServiceProbe copy() {
        ServiceProbe probe = new ServiceProbe();
        probe.setCpuUsage(cpuUsage);
        probe.setServiceName(serviceName);
        probe.setMessageTypeId(messageTypeId);
        return probe;
    }
}
