package tr.edu.boun.cmpe.scn;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TCP;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

/**
 * Created by esinka on 2/6/2017.
 */
public class FlowUtils {

    public static TrafficTreatment.Builder createDefaultTrafficTreatmentBuilder(PortNumber outPort) {
        TrafficTreatment.Builder defaultTreatmentBuilder = DefaultTrafficTreatment.builder();
        defaultTreatmentBuilder.setOutput(outPort);
        return defaultTreatmentBuilder;
    }

    public static FlowRule createFlowRule(TrafficSelector.Builder selectorBuilder,
                                          TrafficTreatment.Builder treatmentBuilder,
                                          DeviceId did, ApplicationId appId, int flowPriority, int flowTimeout) {
        return DefaultFlowRule.builder()
                .forDevice(did)
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(flowPriority)
                .fromApp(appId)
                .makeTemporary(flowTimeout)
                .build();
    }

    public static SelectorValue createSelectorValue(Ethernet ethPkt, Boolean reverse, int timeoutSecs) {

        SelectorValue selectorValue = new SelectorValue(timeoutSecs);
        selectorValue.setMacSrc(reverse ? ethPkt.getDestinationMAC() : ethPkt.getSourceMAC());
        selectorValue.setMacDst(reverse ? ethPkt.getSourceMAC() : ethPkt.getDestinationMAC());
        if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
            IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

            selectorValue.setIpSrc(reverse ? Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(), Ip4Prefix.MAX_MASK_LENGTH) : Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(), Ip4Prefix.MAX_MASK_LENGTH));
            selectorValue.setIpDst(reverse ? Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(), Ip4Prefix.MAX_MASK_LENGTH) : Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(), Ip4Prefix.MAX_MASK_LENGTH));

            byte ipv4Protocol = ipv4Packet.getProtocol();
            if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                selectorValue.setTcpSrc(reverse ? TpPort.tpPort(tcpPacket.getDestinationPort()) : TpPort.tpPort(tcpPacket.getSourcePort()));
                selectorValue.setTcpDst(reverse ? TpPort.tpPort(tcpPacket.getSourcePort()) : TpPort.tpPort(tcpPacket.getDestinationPort()));
            } else if (ipv4Protocol == IPv4.PROTOCOL_UDP) {
                UDP udpPacket = (UDP) ipv4Packet.getPayload();
                selectorValue.setUdpSrc(reverse ? TpPort.tpPort(udpPacket.getDestinationPort()) : TpPort.tpPort(udpPacket.getSourcePort()));
                selectorValue.setUdpDst(reverse ? TpPort.tpPort(udpPacket.getSourcePort()) : TpPort.tpPort(udpPacket.getDestinationPort()));
            }
        }
        return selectorValue;
    }

    public static String buildServiceLocationKey(DeviceId deviceId, PortNumber port, MacAddress macAddress, int udpPort) {
        return new StringBuilder()
                .append(deviceId.toString())
                .append(port.toLong())
                .append(macAddress.toLong())
                .append(udpPort).toString();
    }

    public static String buildServiceLocationKey(ServiceInfo serviceInfo) {
        return new StringBuilder()
                .append(serviceInfo.getDeviceId())
                .append(serviceInfo.getDevicePort())
                .append(serviceInfo.getHostId().mac().toLong())
                .append(serviceInfo.getPort()).toString();
    }
}
