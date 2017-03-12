package tr.edu.boun.cmpe.scn;

import com.google.gson.Gson;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.EthType;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import tr.edu.boun.cmpe.scn.api.common.Constants;
import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.message.ScnMessage;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.api.message.ServiceProbe;
import tr.edu.boun.cmpe.scn.api.message.ServiceUp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by esinka on 1/5/2017.
 */
@Component(immediate = true)
@Service
public class ScnController implements ScnService {
    private final Logger log = getLogger(getClass());

    private static final int DEFAULT_PRIORITY = 10;
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_PROBE_PERIOD_MILLISECONDS = 1000;
    private static final String SENDER_MAC_ADDRESS = "00:00:00:00:00:01";

    private static byte packetTTL = (byte) 127;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Property(name = "probeEnabled", boolValue = true, label = "Enable ServiceProbe emitter; default is false")
    boolean probeEnabled = true;
    @Property(name = "probePeriod", intValue = DEFAULT_PROBE_PERIOD_MILLISECONDS, label = "Configure ServiceProbe period in milliseconds; default is 1000 milliseconds")
    private int probePeriod = DEFAULT_PROBE_PERIOD_MILLISECONDS;

    @Property(name = "macAddress", value = SENDER_MAC_ADDRESS, label = "macAddress value; default is 00:00:00:00:00:01")
    private String macAddress = SENDER_MAC_ADDRESS;
    @Property(name = "ipAddress", value = "10.0.0.190", label = "ipAddress value; default is 10.0.0.190")
    private String ipAddress = "10.0.0.190";

    private ApplicationId appId;
    ScnPacketProcessor processor = new ScnPacketProcessor();
    HostListener hostListener = new InternalHostListener();

    int hardTimeoutSecs = 10;

    ScheduledExecutorService scheduler;

    public static final int SCN_DST_IP_V4 = IPv4.toIPv4Address(Constants.SCN_BROADCAST_ADDRESS);

    private static final ConcurrentHashMap<String, Services> serviceNameToInstancesMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, SelectorValue> pathIds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ServiceInfo> serviceLocationToInstanceMap = new ConcurrentHashMap<>();

    @Activate
    public void activate(ComponentContext context) {
        cfgService.registerProperties(getClass());
        appId = coreService.registerApplication("org.onosproject.scn");
        packetService.addProcessor(processor, PacketProcessor.director(0));
        requestPackets();
        hostService.addListener(hostListener);
        log.info("ScnHandler activated");
        scheduler = Executors.newScheduledThreadPool(2);
        readComponentConfiguration(context);

        scheduler.scheduleWithFixedDelay(pathIdChecker, (long) 3, (long) 3, TimeUnit.SECONDS);
        //check service probes per 10 milliseconds
        scheduler.scheduleWithFixedDelay(probeChecker, (long) 3, (long) 10, TimeUnit.MILLISECONDS);
    }

    final Runnable pathIdChecker = new Runnable() {
        public void run() {
            clearPathIdMap();
        }
    };

    final Runnable probeChecker = new Runnable() {
        @Override
        public void run() {
            checkProbes();
        }
    };

    @Deactivate
    protected void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        packetService.removeProcessor(processor);
        cancelPackets();
        hostService.removeListener(hostListener);
        scheduler.shutdown();
        log.info("ScnHandler stopped");
    }

    private void readComponentConfiguration(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        Boolean probeEnabledCfg =
                Tools.isPropertyEnabled(properties, "probeEnabled");
        if (probeEnabledCfg == null) {
            log.info("probeEnabled port is not configured, using current value of {}", probeEnabled);
        } else {
            probeEnabled = probeEnabledCfg;
            log.info("Configured.probeEnabled is {}", probeEnabled ? "enabled" : "disabled");
        }
    }

    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);
    }

    /**
     * Request packet in via PacketService.
     */
    private void requestPackets() {

        TrafficSelector.Builder selectorServer = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP);

        packetService.requestPackets(selectorServer.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * Cancel requested packets in via packet service.
     */
    private void cancelPackets() {
        TrafficSelector.Builder selectorServer = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_UDP);

        packetService.cancelPackets(selectorServer.build(), PacketPriority.REACTIVE, appId);
    }

    @Override
    public Iterable<ServiceInfo> getServiceInstances() {
        Set<ServiceInfo> serviceSet = new HashSet<>();
        Collection<Services> values = serviceNameToInstancesMap.values();
        values.forEach(services1 -> {
                           services1.getServices().forEach(serviceInfo -> serviceSet.add(serviceInfo));
                       }
        );
        return serviceSet;
    }

    // Indicates whether this is a control packet, e.g. LLDP, BDDP
    private boolean isControlPacket(Ethernet eth) {
        short type = eth.getEtherType();
        return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
    }

    void clearPathIdMap() {
        List<String> keys = new ArrayList<>();
        Collection<SelectorValue> values = pathIds.values();
        values.forEach(selectorValue -> {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            if (timeInMillis >= selectorValue.getExpire()) {
                keys.add(selectorValue.getKey());
            }
        });
        if (!keys.isEmpty()) {
            log.info("FLows will be removed -> {}", keys);
            for (String key : keys) {
                SelectorValue removed = pathIds.remove(key);
                if (removed != null) {
                    removeFlowsFromDevices(removed.getFlowRules());
                }
            }
        }
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

                log.info("----> RECEIVED: {} src={} dst={}", context.inPacket().receivedFrom(), eth.getSourceMAC(), eth.getDestinationMAC());

                if (isScnMessage(context.inPacket().receivedFrom(), eth)) {
                    IPv4 iPv4 = (IPv4) eth.getPayload();
                    UDP udp = (UDP) iPv4.getPayload();
                    if (udp.getPayload() == null) {
                        return;
                    }
                    Gson gson = new Gson();
                    String payload = getPayload(udp.getPayload());
                    log.info("SCN message received from {} --> {}", context.inPacket().receivedFrom(), payload);

                    ScnMessage scnMessage = gson.fromJson(payload, ScnMessage.class);
                    ScnMessageType scnMessageType = ScnMessageType.valueOf(scnMessage.getMessageTypeId());

                    switch (scnMessageType) {
                        case UP:
                            ServiceUp serviceUp = gson.fromJson(payload, ServiceUp.class);
                            processServiceUp(context, serviceUp);
                            context.block();
                            break;
                        case INTEREST:
                            ServiceInterest interest = gson.fromJson(payload, ServiceInterest.class);
                            processServiceInterest(context, interest);
                            context.block();
                            break;
                        case DATA:
                            ServiceData data = gson.fromJson(payload, ServiceData.class);
                            processServiceData(context, data);
                            context.block();
                            break;
                        case PROBE:
                            //TODO
                    }
                }
            }
        }
    }

    String getPayload(IPacket payload) {
        return new String(payload.serialize(), Charset.forName(Constants.UTF8));
    }

    boolean isScnMessage(ConnectPoint connectPoint, Ethernet eth) {
        IPv4 iPv4 = (IPv4) eth.getPayload();
        if (iPv4.getDestinationAddress() == SCN_DST_IP_V4 &&
                iPv4.getProtocol() == IPv4.PROTOCOL_UDP) {
            UDP udp = (UDP) iPv4.getPayload();
            return udp.getSourcePort() == Constants.SCN_SERVICE_PORT ||
                    udp.getSourcePort() == Constants.SCN_CLIENT_PORT ||
                    udp.getDestinationPort() == Constants.SCN_SERVICE_PORT ||
                    udp.getDestinationPort() == Constants.SCN_CLIENT_PORT ||
                    fromService(connectPoint, eth, udp.getSourcePort());
        } else if (iPv4.getProtocol() == IPv4.PROTOCOL_UDP) {
            UDP udp = (UDP) iPv4.getPayload();
            //coming from a live service instance?
            return fromService(connectPoint, eth, udp.getSourcePort());
        }
        return false;
    }

    boolean fromService(ConnectPoint connectPoint, Ethernet ethernet, int srcUdpPort) {
        String locationKey = FlowUtils.buildServiceLocationKey(connectPoint.deviceId(), connectPoint.port(), ethernet.getSourceMAC(), srcUdpPort);
        ServiceInfo serviceInfo = serviceLocationToInstanceMap.get(locationKey);
        log.info("Checking location key: {}", locationKey);
        return serviceInfo == null ? false : true;
    }

    private void processServiceUp(PacketContext context, ServiceUp payload) {
        log.info("SERVICE UP message received. SERVICE NAME={}, PORT={}, Src MAC={}",
                 payload.getServiceName(), payload.getServicePort(), context.inPacket().parsed().getSourceMAC());

        ConnectPoint connectPoint = context.inPacket().receivedFrom();
        HostId hostId = HostId.hostId(context.inPacket().parsed().getSourceMAC());
        ServiceInfo serviceInfo = parseServiceInfo(connectPoint, hostId, payload);
        Services services = ScnController.serviceNameToInstancesMap.get(serviceInfo.getName());
        if (services == null) {
            services = new Services();
        }
        services.addInstance(connectPoint.deviceId(), connectPoint.port(), serviceInfo);
        ScnController.serviceNameToInstancesMap.put(serviceInfo.getName(), services);

        String locationKey = FlowUtils.buildServiceLocationKey(serviceInfo);
        log.info("Service location added: {}", locationKey);
        serviceLocationToInstanceMap.put(locationKey, serviceInfo);
    }

    private ServiceInfo parseServiceInfo(ConnectPoint receivedFrom, HostId src, ServiceUp payload) {
        ServiceInfo service = new ServiceInfo();
        service.setPort(payload.getServicePort());
        service.setName(payload.getServiceName().trim());
        service.setDeviceId(receivedFrom.deviceId().toString());
        service.setDevicePort(receivedFrom.port().toLong());
        service.setHostId(src);
        if (probeEnabled) {
            Date expire = DateUtils.dateAddMilliSeconds(probePeriod);
            service.setProbeExpiresAt(expire.getTime());
            log.info("ServiceProbe will be send at {}", expire);
        }
        return service;
    }

    /**
     * @param receivedFrom
     * @param dst
     * @return true if source and destination hosts are on the same switch
     */
    private boolean isOnTheSameSwitch(ConnectPoint receivedFrom, Host dst) {
        return receivedFrom.deviceId().equals(dst.location().deviceId()) &&
                !receivedFrom.port().equals(dst.location().port());
    }

    private void processServiceData(PacketContext context, ServiceData data) {
        Ethernet ethernet = context.inPacket().parsed();
        //find dst host
        HostId hostId = HostId.hostId(ethernet.getDestinationMAC());
        Host dst = hostService.getHost(hostId);

        if (dst == null) {
            log.warn("Destination host[{}] is not found.", ethernet.getDestinationMAC());
            return;
        }

        boolean onTheSameSwitch = isOnTheSameSwitch(context.inPacket().receivedFrom(), dst);
        ServiceComparable serviceComparable = new ServiceComparable();
        serviceComparable.setDestination(dst);

        if (onTheSameSwitch) {
            serviceComparable.setDistance(0);
        } else {
            Path path = findPath(context, dst);
            if (path == null) {
                log.warn("No path found for ServiceData! for Src:{} to dst:{} and Service:{}",
                         ethernet.getSourceMAC(), ethernet.getDestinationMAC(), data.getServiceName());
                return;
            }
            serviceComparable.setPath(path);
            serviceComparable.setDistance(path.links().size());
        }
        log.info("Processing ServiceData. {}", serviceComparable);
        installPath(context, serviceComparable, false);
    }

    private void processServiceInterest(PacketContext context, ServiceInterest interest) {
        //find service instances
        Services services = ScnController.serviceNameToInstancesMap.get(interest.getServiceName());

        if (services == null) {
            log.warn("No running service instance found for {} service!", interest.getServiceName());
            return;
        }

        List<ServiceComparable> comparableServiceList = getComparableServiceList(context, services);

        if (comparableServiceList.isEmpty()) {
            log.warn("No path found for service {}", interest.getServiceName());
            return;
        }

        //sort the list into ascending order considering distance
        Collections.sort(comparableServiceList);
        log.info("Available service list after sorting: {}", comparableServiceList);
        //get the closest one
        ServiceComparable serviceComparable = comparableServiceList.get(0);
        installPath(context, serviceComparable, true);
    }

    private void handleFirstEdgeSwitch(Ethernet eth, TrafficSelector.Builder selectorBuilder, TrafficTreatment.Builder treatmentBuilder,
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

            treatmentBuilder
                    .setEthDst(dstMac)
                    .setIpDst(dstAddress)
                    .setUdpDst(TpPort.tpPort(udpDstPort))
                    .setOutput(outPort);

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

            treatmentBuilder
                    //.setEthSrc(MacAddress.BROADCAST)
                    //.setIpSrc(IpAddress.valueOf(SCN_DST_IP_V4))
                    .setUdpSrc(TpPort.tpPort(Constants.SCN_SERVICE_PORT))
                    .setOutput(outPort);
        }
    }

    private void handleCenterSwitch(TrafficSelector.Builder selectorBuilder, TrafficTreatment.Builder treatmentBuilder,
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

    private void installPath(PacketContext context, ServiceComparable serviceComparable, boolean interest) {
        Ethernet eth = context.inPacket().parsed();
        IPv4 iPv4 = (IPv4) eth.getPayload();
        UDP udp = (UDP) iPv4.getPayload();

        Host dst = serviceComparable.getDestination();

        MacAddress srcMac = eth.getSourceMAC();

        //get Source host
        HostId srcHostId = HostId.hostId(srcMac);
        Host src = hostService.getHost(srcHostId);

        if (src == null) {
            log.warn("Source host is not found! {}", srcHostId);
            return;
        }

        MacAddress dstMac = dst.mac();
        IpAddress dstAddress = dst.ipAddresses().iterator().next();
        IpAddress srcAddress = IpAddress.valueOf(iPv4.getSourceAddress());
        int udpSrcPort = udp.getSourcePort();
        //udp destination is considered as service port for interest messages
        int udpDstPort = interest ? serviceComparable.getServiceInfo().getPort() : udp.getDestinationPort();

        SelectorValue selectorValue = FlowUtils.createSelectorValue(context.inPacket().parsed(), false, hardTimeoutSecs);
        selectorValue.setIpDst(ipPrefix(dstAddress.getIp4Address().toInt()));
        selectorValue.setMacDst(dstMac);
        selectorValue.setUdpDst(TpPort.tpPort(udpDstPort));

        SelectorValue value = pathIds.get(selectorValue.getKey());
        if (value != null) {
            log.info("Path already added. Doing nothing. {}", selectorValue.getKey());
            return;
        }

        PortNumber inPort;
        PortNumber outPort;
        DeviceId did;

        TrafficSelector.Builder selectorBuilder;
        TrafficTreatment.Builder treatmentBuilder;

        List<FlowRule> flowRuleList = new ArrayList<>();

        if (serviceComparable.getDistance() == 0) {
            //at the same switch
            selectorBuilder = DefaultTrafficSelector.builder();
            treatmentBuilder = DefaultTrafficTreatment.builder();

            inPort = src.location().port();
            outPort = dst.location().port();
            did = context.inPacket().receivedFrom().deviceId();

            handleFirstEdgeSwitch(eth, selectorBuilder, treatmentBuilder,
                                  srcMac, dstMac,
                                  inPort, outPort, srcAddress, dstAddress,
                                  udpSrcPort, udpDstPort, interest);
            FlowRule flowRule = FlowUtils.createFlowRule(selectorBuilder, treatmentBuilder, did, appId, DEFAULT_PRIORITY, DEFAULT_TIMEOUT);
            flowRuleList.add(flowRule);
        } else {
            //create path
            Path path = serviceComparable.getPath();

            Link link;
            Link preLink = null;

            for (int i = 0; i <= path.links().size(); i++) {
                selectorBuilder = DefaultTrafficSelector.builder();
                treatmentBuilder = DefaultTrafficTreatment.builder();

                if (i == 0) {
                    link = path.links().get(0);
                    inPort = src.location().port();
                    outPort = link.src().port();
                    did = context.inPacket().receivedFrom().deviceId();

                    handleFirstEdgeSwitch(eth, selectorBuilder, treatmentBuilder,
                                          srcMac, dstMac,
                                          inPort, outPort,
                                          srcAddress, dstAddress,
                                          udpSrcPort, udpDstPort, interest);

                } else if (i == path.links().size()) {
                    if (preLink == null) {
                        throw new RuntimeException("The last link has some problems!!!");
                    }
                    link = preLink;
                    inPort = link.dst().port();
                    outPort = dst.location().port();
                    did = link.dst().deviceId();

                    handleCenterSwitch(selectorBuilder, treatmentBuilder,
                                       srcMac, dstMac,
                                       inPort, outPort,
                                       srcAddress, dstAddress,
                                       udpSrcPort, udpDstPort);
                } else {
                    link = path.links().get(i);
                    if (preLink == null || preLink.dst() == null) {
                        log.error("PreLink or preLink destination is null. Returning..");
                        return;
                    }
                    inPort = preLink.dst().port();
                    outPort = link.src().port();
                    did = link.src().deviceId();

                    handleCenterSwitch(selectorBuilder, treatmentBuilder,
                                       srcMac, dstMac,
                                       inPort, outPort,
                                       srcAddress, dstAddress,
                                       udpSrcPort, udpDstPort);
                }
                FlowRule flowRule = FlowUtils.createFlowRule(selectorBuilder, treatmentBuilder, did, appId, DEFAULT_PRIORITY, DEFAULT_TIMEOUT);
                flowRuleList.add(flowRule);
                preLink = link;
            }
        }
        selectorValue.setFlowRules(flowRuleList);
        pathIds.put(selectorValue.getKey(), selectorValue);
        log.info("PathId saved:{}", selectorValue.getKey());
        installRule(context, flowRuleList, dst.location().port());
    }

    private boolean serviceStillThere(HostLocation hostLocation, ServiceInfo serviceInfo) {
        return hostLocation.deviceId().toString().equals(serviceInfo.getDeviceId())
                && hostLocation.port().toLong() == serviceInfo.getDevicePort();
    }

    private List<ServiceComparable> getComparableServiceList(PacketContext context, Services services) {
        final List<ServiceComparable> comparableList = new ArrayList<>();

        ServiceComparable serviceComparable;
        for (ServiceInfo serviceInfo : services.getServices()) {
            Host serviceHost = hostService.getHost(serviceInfo.getHostId());

            if (serviceHost == null) {
                log.warn("Service host is not available any more. Thus, removed from the service list. {}", serviceInfo);
                ScnController.serviceNameToInstancesMap.remove(serviceInfo);
                continue;
            }

            if (!serviceStillThere(serviceHost.location(), serviceInfo)) {
                log.warn("Service host location can not be verified. Thus, removed from the service list. {}, Host location={}", serviceInfo, serviceHost.location());
                ScnController.serviceNameToInstancesMap.remove(serviceInfo);
                continue;
            }

            // Are we on an edge switch that our destination is on?
            boolean onTheSameSwitch = isOnTheSameSwitch(context.inPacket().receivedFrom(), serviceHost);
            if (onTheSameSwitch) {
                serviceComparable = new ServiceComparable();
                serviceComparable.setDistance(0);
                serviceComparable.setServiceInfo(serviceInfo);
                serviceComparable.setDestination(serviceHost);
                comparableList.add(serviceComparable);
                break;
            } else {
                //find path
                Path path = findPath(context, serviceHost);
                if (path == null) {
                    log.warn("No path found for Src:{} to dst:{} and Service:{}",
                             context.inPacket().parsed().getSourceMAC(), serviceHost.mac(), serviceInfo.getName());
                    continue;
                }
                serviceComparable = new ServiceComparable();
                serviceComparable.setDistance(path.links().size());
                serviceComparable.setPath(path);
                serviceComparable.setServiceInfo(serviceInfo);
                serviceComparable.setDestination(serviceHost);
                comparableList.add(serviceComparable);
            }

        }
        return comparableList;
    }

    private Path findPath(PacketContext context, Host dst) {
        log.info("Finding path from src {} to dest {}", context.inPacket().parsed().getSourceMAC(), dst.mac());
        Set<Path> paths = topologyService.getPaths(topologyService.currentTopology(),
                                                   context.inPacket().receivedFrom().deviceId(),
                                                   dst.location().deviceId());
        // Otherwise, pick a path that does not lead back to where we
        // came from; if no such path, flood and bail.
        return pickForwardPathIfPossible(paths, context.inPacket().receivedFrom().port());
    }

    // Selects a path from the given set that does not lead back to the
    // specified port if possible.
    private Path pickForwardPathIfPossible(Set<Path> paths, PortNumber notToPort) {
        Path lastPath = null;
        for (Path path : paths) {
            lastPath = path;
            if (!path.src().port().equals(notToPort)) {
                return path;
            }
        }
        return lastPath;
    }

    private void installRule(PacketContext context, List<FlowRule> ruleList, PortNumber out) {
        for (FlowRule rule : ruleList) {

            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(rule.selector())
                    .withTreatment(rule.treatment())
                    .withPriority(DEFAULT_PRIORITY)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(DEFAULT_TIMEOUT)
                    .add();
            flowObjectiveService.forward(rule.deviceId(), forwardingObjective);
            log.info("Rule is processed: {}", rule);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        packetOut(context, PortNumber.TABLE);
    }


    // Sends a packet out the specified port.
    private void packetOut(PacketContext context, PortNumber portNumber) {
        context.treatmentBuilder().setOutput(portNumber);
        context.send();
        log.info("Packet out to port. {}", portNumber);
    }


    private Ip4Prefix ipPrefix(int ipAddress) {
        return Ip4Prefix.valueOf(ipAddress, Ip4Prefix.MAX_MASK_LENGTH);
    }

    private class InternalHostListener implements HostListener {

        @Override
        public void event(HostEvent hostEvent) {
            switch (hostEvent.type()) {
                case HOST_REMOVED:
                    onHostRemoved(hostEvent);
                    break;
                default:
            }
        }

        private void onHostRemoved(HostEvent hostEvent) {
            if (log.isInfoEnabled()) {
                log.info("{} event occurred. Event:{}", hostEvent.type(), hostEvent.toString());
            }
            Host host = hostEvent.subject();
            //find related service
            List<ServiceInfo> instancesToBeRemoved = new ArrayList<>();
            serviceNameToInstancesMap.values().forEach(services -> {
                //remove the instance at this location if any
                ServiceInfo serviceInfo = services.removeInstance(host.location().deviceId(), host.location().port());
                if (serviceInfo != null) {
                    String locationKey = FlowUtils.buildServiceLocationKey(serviceInfo);
                    serviceLocationToInstanceMap.remove(locationKey);
                }
            });
        }
    }

    private void removeFlowsFromDevices(List<FlowRule> rules) {
        if (rules != null) {
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            for (FlowRule rule : rules) {
                ops = ops.remove(rule);
            }
            flowRuleService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("flow rules successfully deleted: " + ops.toString() + ", " + rules.toString());
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("error during removing flow rules: " + ops.toString() + ", " + rules.toString());
                }
            }));
        }
    }

    private void checkProbes() {
        serviceNameToInstancesMap.values().forEach(services -> {
            services.getServices().forEach(serviceInfo -> {
                if (probeEnabled && serviceInfo.getProbeExpiresAt() != null) {
                    long currentTime = new Date().getTime();
                    if (exceeds(currentTime, serviceInfo.getProbeExpiresAt())) {
                        sendServiceProbe(serviceInfo);
                    }
                }
            });
        });
    }

    private boolean exceeds(long date1, long date2) {
        return date1 >= date2;
    }

    private void sendServiceProbe(ServiceInfo serviceInfo) {
        log.info("Sending probe to {} at {}:{}", serviceInfo.getName(), serviceInfo.getDeviceId(), serviceInfo.getDevicePort());
        Host serviceHost = hostService.getHost(serviceInfo.getHostId());

        if (!serviceStillThere(serviceHost.location(), serviceInfo)) {
            log.warn("Service host location can not be verified. Thus, not send ServiceProbe. {}, Host location={}", serviceInfo, serviceHost.location());
            return;
        }
        //build probe message
        ServiceProbe serviceProbe = new ServiceProbe();
        serviceProbe.setMessageTypeId(ScnMessageType.PROBE.getId());
        serviceProbe.setServiceName(serviceInfo.getName());
        Ethernet eth = new Ethernet();

        eth.setDestinationMACAddress(serviceHost.mac().toBytes())
                .setSourceMACAddress(macAddress)
                .setEtherType(Ethernet.TYPE_IPV4).setPayload(serviceProbe);

        IPv4 iPv4 = new IPv4();
        iPv4.setSourceAddress(ipAddress);
        iPv4.setDestinationAddress(serviceHost.ipAddresses().iterator().next().getIp4Address().toInt());
        iPv4.setTtl(packetTTL);
        iPv4.setPayload(serviceProbe);
        eth.setPayload(iPv4);

        TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(serviceHost.location().port()).build();
        OutboundPacket packet = new DefaultOutboundPacket(serviceHost.location().deviceId(),
                                                          treatment, ByteBuffer.wrap(eth.serialize()));

        packetService.emit(packet);
        log.info("ServiceProbe message has been sent to {} at {}", serviceInfo.getName(), serviceHost);
    }
}