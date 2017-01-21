package tr.edu.boun.cmpe.scn;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.EthType;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by esinka on 1/5/2017.
 */
@Component(immediate = true)
@Service
public class ScnHandler implements ScnService {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ApplicationId appId;
    ScnPacketProcessor processor = new ScnPacketProcessor();

    public static final int SCN_SOURCE_PORT = 1742;
    public static final int SCN_DST_IP_V4 = IPv4.toIPv4Address("255.255.255.255");

    private static ConcurrentHashMap<String, Services> services = new ConcurrentHashMap<>();
    ;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("boun.cmpe.scn");
        packetService.addProcessor(processor, PacketProcessor.director(0));
        requestPackets();
        log.info("ScnHandler activated");
    }

    @Deactivate
    protected void deactivate() {
        packetService.removeProcessor(processor);
        cancelPackets();
        log.info("ScnHandler stopped");
    }

    /**
     * Request packet in via PacketService.
     */
    private void requestPackets() {

        TrafficSelector.Builder selectorServer = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpSrc(TpPort.tpPort(SCN_SOURCE_PORT))
                .matchIPDst(Ip4Prefix.valueOf(SCN_DST_IP_V4, Ip4Prefix.MAX_MASK_LENGTH));

        packetService.requestPackets(selectorServer.build(), PacketPriority.CONTROL, appId);
    }

    /**
     * Cancel requested packets in via packet service.
     */
    private void cancelPackets() {
        TrafficSelector.Builder selectorServer = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP)
                .matchUdpSrc(TpPort.tpPort(SCN_SOURCE_PORT))
                .matchIPDst(Ip4Prefix.valueOf(SCN_DST_IP_V4, Ip4Prefix.MAX_MASK_LENGTH));

        packetService.cancelPackets(selectorServer.build(), PacketPriority.CONTROL, appId);
    }

    @Override
    public Iterable<ServiceInfo> getServiceInstances() {
        Set<ServiceInfo> serviceSet = new HashSet<>();
        Collection<Services> values = services.values();
        values.forEach(services1 -> {
                           services1.getServices().forEach(serviceInfo -> serviceSet.add(serviceInfo));
                       }
        );
        return serviceSet;
    }

    private class ScnPacketProcessor implements PacketProcessor {

        /**
         * Processes the inbound packet as specified in the given context.
         *
         * @param context packet processing context
         */
        @Override
        public void process(PacketContext context) {
            if (context.isHandled()) {
                return;
            }

            Ethernet eth = context.inPacket().parsed();
            if (eth == null) {
                return;
            }
            // Bail if this is deemed to be a control packet.
            if (isControlPacket(eth)) {
                return;
            }

            if (EthType.EtherType.lookup(eth.getEtherType()).equals(EthType.EtherType.IPV4)) {
                log.info("==>IPV4 packet received. SrcMAC={}, DstMAC={}", eth.getSourceMAC(), eth.getDestinationMAC());
                if (serviceUpMessage(eth)) {
                    IPv4 iPv4 = (IPv4) eth.getPayload();
                    UDP udp = (UDP) iPv4.getPayload();
                    log.info("==>ServiceUP message received. SrcMAC={}, DstMAC={}", eth.getSourceMAC(), eth.getDestinationMAC());
                    if (udp.getPayload() != null) {
                        String data = new String(udp.getPayload().serialize(), Charset.forName("UTF-8"));
                        log.info("==>Payload={}", data);
                        processServiceUp(context, data);
                        context.block();
                    }
                }
            }
        }
    }

    private boolean serviceUpMessage(Ethernet eth) {
        //dst ip should be zero and udp src should be 1742
        IPv4 iPv4 = (IPv4) eth.getPayload();
        if (iPv4.getDestinationAddress() == SCN_DST_IP_V4 &&
                iPv4.getProtocol() == IPv4.PROTOCOL_UDP) {
            UDP udp = (UDP) iPv4.getPayload();
            log.info("==> DstIP={} UdpSrcPort={} UdpDstPort={}", iPv4.getDestinationAddress(), udp.getSourcePort(), udp.getDestinationPort());
            return udp.getSourcePort() == SCN_SOURCE_PORT ? true : false;
        }

        return false;
    }

    private void processServiceUp(PacketContext context, String payload) {
        ConnectPoint connectPoint = context.inPacket().receivedFrom();
        ServiceInfo serviceInfo = parseServiceInfo(connectPoint, payload);
        Services services = ScnHandler.services.get(serviceInfo.getName());
        if (services == null) {
            services = new Services();
        }
        services.addInstance(connectPoint.deviceId(), connectPoint.port(), serviceInfo);
        ScnHandler.services.put(serviceInfo.getName(), services);
    }


    private ServiceInfo parseServiceInfo(ConnectPoint receivedFrom, String payload) {
        String[] split = payload.trim().split(":");
        String serviceName = split[0];
        int servicePort = Integer.parseInt(split[1]);

        ServiceInfo service = new ServiceInfo();
        service.setPort(servicePort);
        service.setName(serviceName);
        service.setDeviceId(receivedFrom.deviceId().toString());
        service.setDevicePort(receivedFrom.port().toLong());
        return service;
    }

    // Indicates whether this is a control packet, e.g. LLDP, BDDP
    private boolean isControlPacket(Ethernet eth) {
        short type = eth.getEtherType();
        return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
    }
}
