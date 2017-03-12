package tr.edu.boun.cmpe.scn.client;

import tr.edu.boun.cmpe.scn.api.message.ServiceData;

/**
 * Created by esinka on 3/1/2017.
 */
public interface IScnListener {

    void received(ServiceData serviceData);
}
