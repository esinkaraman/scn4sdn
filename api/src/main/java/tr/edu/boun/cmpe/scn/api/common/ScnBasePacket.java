package tr.edu.boun.cmpe.scn.api.common;

import com.google.gson.Gson;
import org.onlab.packet.BasePacket;
import org.onlab.packet.IPacket;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by esinka on 3/12/2017.
 * Each SCN message must extend this base class.
 */
public class ScnBasePacket extends BasePacket {

    @Override
    public byte[] serialize() {
        byte[] payload = null;
        Gson gson = new Gson();

        try {
            payload = gson.toJson(this, this.getClass()).getBytes(Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unable to serialize the packet " + this.getClass());
            e.printStackTrace();
        }

        return payload == null ? new byte[0] : payload;
    }

    @Override
    public IPacket deserialize(final byte[] data, final int offset,
                               final int length) {
        final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        try {
            String received = new String(bb.array(), Constants.UTF8);
            Gson gson = new Gson();
            return gson.fromJson(received.trim(), this.getClass());

        } catch (UnsupportedEncodingException e) {
            System.err.println("Unable to deserialize the packet " + this.getClass());
            e.printStackTrace();
        }
        return null;
    }
}
