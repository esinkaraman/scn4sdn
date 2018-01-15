package tr.edu.boun.cmpe.scn;

import org.onosproject.net.Host;
import org.onosproject.net.Path;
import tr.edu.boun.cmpe.scn.api.common.ServiceInfo;

/**
 * Created by esinka on 2/6/2017.
 */
public class ServiceComparable implements Comparable<ServiceComparable> {

    public enum CompareBy {
        DISTANCE, CPU_USAGE
    }

    private ServiceInfo serviceInfo;
    private int distance = 0;
    private Path path;
    private Host destination;
    private CompareBy compareBy = CompareBy.DISTANCE;
    private long cpuUsage = 0l;

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

    public CompareBy getCompareBy() {
        return compareBy;
    }

    public void setCompareBy(CompareBy compareBy) {
        this.compareBy = compareBy;
    }

    public long getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(long cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    @Override
    public int compareTo(ServiceComparable o) {
        if (compareBy.equals(CompareBy.CPU_USAGE)) {
            return Long.compare(this.cpuUsage, o.getCpuUsage());
        } else {
            return Integer.compare(this.distance, o.getDistance());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceComparable{");
        sb.append("serviceInfo=").append(serviceInfo);
        sb.append(", distance=").append(distance);
        sb.append(", path=").append(path);
        sb.append(", destination=").append(destination);
        sb.append(", compareBy=").append(compareBy);
        sb.append(", cpuUsage=").append(cpuUsage);
        sb.append('}');
        return sb.toString();
    }
}
