package tr.edu.boun.cmpe.scn.service.demo;

import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.service.worker.IServiceInterestListener;

/**
 * Created by esinka on 3/22/2017.
 */
public class InterestListener implements IServiceInterestListener {
    @Override
    public ServiceData processInterest(ServiceInterest serviceInterest) {
        return prepareResponse(serviceInterest);
    }

    private ServiceData prepareResponse(ServiceInterest interest) {
        ServiceData serviceData = new ServiceData();
        serviceData.setServiceName(interest.getServiceName());
        serviceData.setMessageTypeId(ScnMessageType.DATA.getId());
        serviceData.setMessageId(interest.getMessageId());
        return serviceData;
    }
}
