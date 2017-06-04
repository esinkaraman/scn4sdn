package tr.edu.boun.cmpe.scn.api.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tr.edu.boun.cmpe.scn.api.common.Constants;

import java.io.UnsupportedEncodingException;

public class ServiceInterest extends ScnMessage {

    protected String messageId;
    protected Arguments arguments;

    /**
     * Gets the value of the messageId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the arguments property.
     *
     * @return possible object is
     * {@link Arguments }
     */
    public Arguments getArguments() {
        return arguments;
    }

    /**
     * Sets the value of the arguments property.
     *
     * @param value allowed object is
     *              {@link Arguments }
     */
    public void setArguments(Arguments value) {
        this.arguments = value;
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

    private ServiceInterest copy() {
        ServiceInterest msg = new ServiceInterest();
        msg.setServiceName(serviceName);
        msg.setMessageTypeId(messageTypeId);
        msg.setMessageId(messageId);
        msg.setArguments(arguments);
        return msg;
    }
}
