package tr.edu.boun.cmpe.scn.app.matrix.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.client.IScnListener;

import java.io.UnsupportedEncodingException;

/**
 * Created by esinka on 3/26/2017.
 */
public class ScnListener implements IScnListener {
    private static final Logger log = LogManager.getLogger(ScnListener.class.getName());

    long start;

    public ScnListener() {
        start = System.nanoTime();
    }

    @Override
    public void received(ServiceData serviceData, String ipAddressClient) {
        long elapsed = System.nanoTime() - start;

        log.info(Tool.elapsedLogForClient(elapsed, ipAddressClient));

        System.out.println("Data: messageId:" + serviceData.getMessageId() +
                                   " elapsed:" + elapsed +
                                   " data:" + deserializeData(serviceData.getData()));
    }

    private String deserializeData(byte[] data) {
        try {
            return new String(data, Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
