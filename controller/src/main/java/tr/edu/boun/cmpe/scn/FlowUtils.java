package tr.edu.boun.cmpe.scn;

import org.onlab.packet.EthType;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TCP;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.slf4j.Logger;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.ServiceInfo;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by esinka on 2/6/2017.
 */
public class FlowUtils {

    private static final Logger log = getLogger(FlowUtils.class);

    public static final int SCN_DST_IP_V4 = IPv4.toIPv4Address(Constants.SCN_BROADCAST_ADDRESS);

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

    public static boolean serviceStillThere(HostLocation hostLocation, ServiceInfo serviceInfo) {
        return hostLocation.deviceId().toString().equals(serviceInfo.getDeviceId())
                && hostLocation.port().toLong() == serviceInfo.getDevicePort();
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

    public static Ip4Prefix ipPrefix(int ipAddress) {
        return Ip4Prefix.valueOf(ipAddress, Ip4Prefix.MAX_MASK_LENGTH);
    }

    public static void populateTreatment(TrafficTreatment.Builder treatmentBuilder, boolean interest, MacAddress dstMac, IpAddress dstAddress,
                                         int udpDstPort, PortNumber outPort) {
        if(interest) {
            treatmentBuilder
                    .setEthDst(dstMac)
                    .setIpDst(dstAddress)
                    .setUdpDst(TpPort.tpPort(udpDstPort))
                    .setOutput(outPort);
        } else {
            //data
            treatmentBuilder
                    //.setEthSrc(MacAddress.BROADCAST)
                    //.setIpSrc(IpAddress.valueOf(SCN_DST_IP_V4))
                    .setUdpSrc(TpPort.tpPort(Constants.SCN_SERVICE_PORT))
                    .setOutput(outPort);
        }
    }

    public static void handleFirstEdgeSwitch(Ethernet eth, TrafficSelector.Builder selectorBuilder, TrafficTreatment.Builder treatmentBuilder,
                                             MacAddress srcMac, MacAddress dstMac,
                                             PortNumber inPort, PortNumber outPort, IpAddress srcAddress,
                                             IpAddress dstAddress, int udpSrcPort, int udpDstPort, boolean interest) {
        if (interest) {
            selectorBuilder
                    .matchInPort(inPort)
                    //.matchEthSrc(srcMac)
                    .matchEthType(EthType.EtherType.IPV4.ethType().toShort())
                    .matchIPSrc(ipPrefix(srcAddress.getIp4Address().toInt()))
                    .matchIPDst(ipPrefix(SCN_DST_IP_V4))
                    .matchIPProtocol(IPv4.PROTOCOL_UDP)
                    .matchUdpDst(TpPort.tpPort(Constants.SCN_SERVICE_PORT))
                    .matchUdpSrc(TpPort.tpPort(udpSrcPort));

        } else {
            //service data
            selectorBuilder.matchInPort(inPort)
                    //.matchEthSrc(srcMac)
                    //.matchEthDst(dstMac)
                    .matchEthType(EthType.EtherType.IPV4.ethType().toShort())
                    .matchIPSrc(ipPrefix(srcAddress.getIp4Address().toInt()))
                    .matchIPDst(ipPrefix(dstAddress.getIp4Address().toInt()))
                    .matchIPProtocol(IPv4.PROTOCOL_UDP)
                    .matchUdpDst(TpPort.tpPort(udpDstPort))
                    .matchUdpSrc(TpPort.tpPort(udpSrcPort));
        }
        populateTreatment(treatmentBuilder,interest,dstMac,dstAddress,udpDstPort,outPort);
    }

    public static void handleCenterSwitch(TrafficSelector.Builder selectorBuilder, TrafficTreatment.Builder treatmentBuilder,
                                          MacAddress srcMac, MacAddress dstMac,
                                          PortNumber inPort, PortNumber outPort, IpAddress srcAddress, IpAddress dstAddress,
                                          int udpSrcPort, int udpDstPort) {
        treatmentBuilder.setOutput(outPort);

        selectorBuilder.matchInPort(inPort)
                //.matchEthSrc(srcMac)
                //.matchEthDst(dstMac)
                .matchEthType(EthType.EtherType.IPV4.ethType().toShort())
                .matchIPSrc(ipPrefix(srcAddress.getIp4Address().toInt()))
                .matchIPDst(ipPrefix(dstAddress.getIp4Address().toInt()))
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpSrc(TpPort.tpPort(udpSrcPort))
                .matchUdpDst(TpPort.tpPort(udpDstPort));
    }

    public static Long parseCpuUsage(String cpuUsageStr) {
        Long cpuUsage = null;
        if (cpuUsageStr != null) {
            try {
                cpuUsage = Long.parseLong(cpuUsageStr);
            } catch (NumberFormatException e) {
                log.info("Unable to parse cpu usage valu", e);
            }
        }
        return cpuUsage;
    }
}
