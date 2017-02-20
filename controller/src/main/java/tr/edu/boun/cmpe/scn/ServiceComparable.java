package tr.edu.boun.cmpe.scn;

import org.onosproject.net.Host;
import org.onosproject.net.Path;

/**
 * Created by esinka on 2/6/2017.
 */
public class ServiceComparable implements Comparable<ServiceComparable> {

    private ServiceInfo serviceInfo;
    private int distance = 0;
    private Path path;
    private Host destination;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Host getDestination() {
        return destination;
    }

    public void setDestination(Host destination) {
        this.destination = destination;
    }

    @Override
    public int compareTo(ServiceComparable o) {
        return Integer.compare(this.distance, o.getDistance());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceComparable{");
        sb.append(serviceInfo);
        sb.append(", distance=").append(distance);
        sb.append(", path=").append(path);
        sb.append(", destination=").append(destination);
        sb.append('}');
        return sb.toString();
    }
}
