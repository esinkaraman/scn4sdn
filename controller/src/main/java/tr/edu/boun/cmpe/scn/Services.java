package tr.edu.boun.cmpe.scn;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by esinka on 1/6/2017.
 */
public class Services {
    private final Logger log = getLogger(getClass());

    private ConcurrentHashMap<String, ServiceInfo> serviceMap = new ConcurrentHashMap<>();

    private String prepareKey(DeviceId deviceId, PortNumber portNumber) {
        return new StringBuilder().append(deviceId.toString()).append(portNumber.name()).toString();
    }

    public void addInstance(DeviceId deviceId, PortNumber portNumber, ServiceInfo serviceInfo) {
        String key = prepareKey(deviceId, portNumber);
        serviceMap.put(key, serviceInfo);
        log.info("Service saved. {}", serviceInfo);
    }

    public Iterable<ServiceInfo> getServices() {
        return Collections.unmodifiableCollection(serviceMap.values());
    }

}
