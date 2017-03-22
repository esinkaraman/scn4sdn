package tr.edu.boun.cmpe.scn.service.worker;

import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;

/**
 * Created by esinka on 3/22/2017.
 */
@FunctionalInterface
public interface IServiceInterestListener {
    ServiceData processInterest(ServiceInterest serviceInterest);
}
