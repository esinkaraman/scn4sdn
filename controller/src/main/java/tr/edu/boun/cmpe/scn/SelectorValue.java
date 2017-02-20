package tr.edu.boun.cmpe.scn;

import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.FlowRule;

import java.util.Calendar;
import java.util.List;

/**
 * Created by gamzeab on 11.01.2017.
 */
public class SelectorValue {

    private MacAddress macSrc;
    private MacAddress macDst;
    private Ip4Prefix ipSrc;
    private Ip4Prefix ipDst;
    private TpPort tcpSrc;
    private TpPort tcpDst;
    private TpPort udpSrc;
    private TpPort udpDst;
    private PortNumber inPort;
    private short etherType;
    private long expire;
    private List<FlowRule> flowRules;

    public SelectorValue(int timeoutSecs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, timeoutSecs);
        expire = cal.getTimeInMillis();
    }

    public long getExpire() {
        return expire;
    }

    public short getEtherType() {
        return etherType;
    }

    public void setEtherType(short etherType) {
        this.etherType = etherType;
    }

    public Ip4Prefix getIpSrc() {
        return ipSrc;
    }

    public void setIpSrc(Ip4Prefix ipSrc) {
        this.ipSrc = ipSrc;
    }

    public Ip4Prefix getIpDst() {
        return ipDst;
    }

    public void setIpDst(Ip4Prefix ipDst) {
        this.ipDst = ipDst;
    }

    public TpPort getTcpSrc() {
        return tcpSrc;
    }

    public void setTcpSrc(TpPort tcpSrc) {
        this.tcpSrc = tcpSrc;
    }

    public TpPort getTcpDst() {
        return tcpDst;
    }

    public void setTcpDst(TpPort tcpDst) {
        this.tcpDst = tcpDst;
    }

    public TpPort getUdpSrc() {
        return udpSrc;
    }

    public void setUdpSrc(TpPort udpSrc) {
        this.udpSrc = udpSrc;
    }

    public TpPort getUdpDst() {
        return udpDst;
    }

    public void setUdpDst(TpPort udpDst) {
        this.udpDst = udpDst;
    }

    public PortNumber getInPort() {
        return inPort;
    }

    public void setInPort(PortNumber inPort) {
        this.inPort = inPort;
    }

    public MacAddress getMacSrc() {
        return macSrc;
    }

    public void setMacSrc(MacAddress macSrc) {
        this.macSrc = macSrc;
    }

    public MacAddress getMacDst() {
        return macDst;
    }

    public void setMacDst(MacAddress macDst) {
        this.macDst = macDst;
    }

    public List<FlowRule> getFlowRules() {
        return flowRules;
    }

    public void setFlowRules(List<FlowRule> flowRules) {
        this.flowRules = flowRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SelectorValue that = (SelectorValue) o;

        if (etherType != that.etherType) {
            return false;
        }
        if (macSrc != null ? !macSrc.equals(that.macSrc) : that.macSrc != null) {
            return false;
        }
        if (macDst != null ? !macDst.equals(that.macDst) : that.macDst != null) {
            return false;
        }
        if (ipSrc != null ? !ipSrc.equals(that.ipSrc) : that.ipSrc != null) {
            return false;
        }
        if (ipDst != null ? !ipDst.equals(that.ipDst) : that.ipDst != null) {
            return false;
        }
        if (tcpSrc != null ? !tcpSrc.equals(that.tcpSrc) : that.tcpSrc != null) {
            return false;
        }
        if (tcpDst != null ? !tcpDst.equals(that.tcpDst) : that.tcpDst != null) {
            return false;
        }
        if (udpSrc != null ? !udpSrc.equals(that.udpSrc) : that.udpSrc != null) {
            return false;
        }
        if (udpDst != null ? !udpDst.equals(that.udpDst) : that.udpDst != null) {
            return false;
        }
        return inPort != null ? inPort.equals(that.inPort) : that.inPort == null;
    }

    @Override
    public int hashCode() {
        int result = macSrc != null ? macSrc.hashCode() : 0;
        result = 31 * result + (macDst != null ? macDst.hashCode() : 0);
        result = 31 * result + (ipSrc != null ? ipSrc.hashCode() : 0);
        result = 31 * result + (ipDst != null ? ipDst.hashCode() : 0);
        result = 31 * result + (tcpSrc != null ? tcpSrc.hashCode() : 0);
        result = 31 * result + (tcpDst != null ? tcpDst.hashCode() : 0);
        result = 31 * result + (udpSrc != null ? udpSrc.hashCode() : 0);
        result = 31 * result + (udpDst != null ? udpDst.hashCode() : 0);
        result = 31 * result + (inPort != null ? inPort.hashCode() : 0);
        result = 31 * result + (int) etherType;
        return result;
    }

    @Override
    public String toString() {
        return "SelectorValue{" +
                "macSrc=" + macSrc +
                ", macDst=" + macDst +
                ", ipSrc=" + ipSrc +
                ", ipDst=" + ipDst +
                ", tcpSrc=" + tcpSrc +
                ", tcpDst=" + tcpDst +
                ", udpSrc=" + udpSrc +
                ", udpDst=" + udpDst +
                ", inPort=" + inPort +
                ", etherType=" + etherType +
                '}';
    }

    public String getKey() {
        return new StringBuilder()
                .append(this.getIpSrc() == null ? "" : "_" + this.getIpSrc().toString())
                .append("_")
                .append(this.getIpDst() == null ? "" : "_" + this.getIpDst().toString())
                .append("_")
                .append(this.getTcpSrc() == null ? "" : "_" + this.getTcpSrc().toString())
                .append("_")
                .append(this.getTcpDst() == null ? "" : "_" + this.getTcpDst().toString())
                .append("_")
                .append(this.getUdpSrc() == null ? "" : "_" + this.getUdpSrc().toString())
                .append("_")
                .append(this.getUdpDst() == null ? "" : "_" + this.getUdpDst().toString()).toString();
    }
}
