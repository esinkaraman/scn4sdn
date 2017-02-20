package tr.edu.boun.cmpe.scn;

import org.onosproject.net.HostId;

import java.util.Date;

/**
 * Created by esinka on 1/6/2017.
 */
public class ServiceInfo implements Cloneable {
    private String name;
    private int port;
    private String deviceId;
    private long devicePort;
    private HostId hostId;
    private Date lastUpdateTime = new Date();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(long devicePort) {
        this.devicePort = devicePort;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public HostId getHostId() {
        return hostId;
    }

    public void setHostId(HostId hostId) {
        this.hostId = hostId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", port=").append(port);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", devicePort=").append(devicePort);
        sb.append(", hostId=").append(hostId);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append('}');
        return sb.toString();
    }
}
