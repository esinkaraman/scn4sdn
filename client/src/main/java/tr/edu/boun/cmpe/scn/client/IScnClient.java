package tr.edu.boun.cmpe.scn.client;

import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

import java.io.IOException;

/**
 * Created by esinka on 2/4/2017.
 */
public interface IScnClient {

    ServiceData sendInterest(ServiceInterest interest, String srcAddress) throws IOException;
}
