package tr.edu.boun.cmpe.scn;

import org.onosproject.net.HostId;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by esinka on 1/6/2017.
 */
public class ServiceInfo implements Cloneable {
    DecimalFormat df = new DecimalFormat("#,###,###");

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
    private long lastReceivedProbeTime = EMPTY_PROBE_TIME;

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
    }

    public long getCpuUsage() {
        return cpuUsage;
    }

    public Long getLastReceivedProbeTime() {
        return lastReceivedProbeTime;
    }

    public void setLastReceivedProbeTime(long lastProbeTime) {
        this.lastReceivedProbeTime = lastProbeTime;
    }

    public boolean probeEverSent() {
        return lastReceivedProbeTime != EMPTY_PROBE_TIME;
    }

    private String format(long number) {
        return df.format(number);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", port=").append(port);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", devicePort=").append(devicePort);
        sb.append(", lastCpuUsageValue=").append(format(lastCpuUsageValue));
        sb.append(", cpuUsage=").append(format(cpuUsage));
        sb.append(", hostId=").append(hostId);
        sb.append(", lastReceivedProbeTime=").append(lastReceivedProbeTime);
        sb.append('}');
        return sb.toString();
    }
}
