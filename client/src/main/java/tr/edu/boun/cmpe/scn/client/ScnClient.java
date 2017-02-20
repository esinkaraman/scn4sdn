package tr.edu.boun.cmpe.scn.client;

import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

import java.io.IOException;

import static tr.edu.boun.cmpe.scn.api.common.Tool.checkNull;

/**
 * Created by esinka on 2/4/2017.
 */
public class ScnClient extends AbstractScnClient implements IScnClient {

    @Override
    public ServiceData sendInterest(ServiceInterest interest, String srcAddress) throws IOException {
        checkNull(interest, "ServiceInterest can not be null");
        checkNull(interest.getServiceName(), "ServiceName can not be null");
        return send(interest, srcAddress, CyclicCounter.getInstance().cyclicallyIncrementAndGet());
    }

}
