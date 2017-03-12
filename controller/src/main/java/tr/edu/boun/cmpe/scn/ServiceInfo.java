package tr.edu.boun.cmpe.scn;

import org.onosproject.net.HostId;

import java.util.Date;

/**
 * Created by esinka on 1/6/2017.
 */
public class ServiceInfo implements Cloneable {
    public static final long EMPTY_PROBE_TIME = -1L;
    private String name;
    private int port;
    private String deviceId;
    private long devicePort;
    private HostId hostId;
    private Date lastUpdateTime = new Date();

    private Long probeExpiresAt;
    private long lastCpuUsageValue = 0l;
    private long cpuUsage = 0l;
    private long lastProbeTime = EMPTY_PROBE_TIME;

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

    public Long getProbeExpiresAt() {
        return probeExpiresAt;
    }

    public void setProbeExpiresAt(Long probeExpiresAt) {
        this.probeExpiresAt = probeExpiresAt;
    }

    public void setLastCpuUsageValue(long cpuUsage) {
        this.cpuUsage = cpuUsage - lastCpuUsageValue;
        this.lastCpuUsageValue = cpuUsage;
        lastProbeTime = System.currentTimeMillis();
    }

    public long getCpuUsage() {
        return cpuUsage;
    }

    public Long getLastProbeTime() {
        return lastProbeTime;
    }

    public void setLastProbeTime(long lastProbeTime) {
        this.lastProbeTime = lastProbeTime;
    }

    public boolean probeEverSent() {
        return lastProbeTime != EMPTY_PROBE_TIME;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", port=").append(port);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", devicePort=").append(devicePort);
        sb.append(", hostId=").append(hostId);
        sb.append(", lastCpuUsageValue=").append(lastCpuUsageValue);
        sb.append(", cpuUsage=").append(cpuUsage);
        sb.append(", lastProbeTime=").append(lastProbeTime);
        sb.append('}');
        return sb.toString();
    }
}
