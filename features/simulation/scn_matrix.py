#!/usr/bin/python

from mininet.cli import CLI
from mininet.log import lg, info
from mininet.net import Mininet
from mininet.node import OVSKernelSwitch
from mininet.topolib import TreeTopo
from mininet.util import ensureRoot
from mininet.clean import cleanup
from mininet.node import Controller, RemoteController, Node
from mininet.node import CPULimitedHost

topo = None
sIndex = 1
hIndex = 1
hostMap = {}
linkMap = {}
switchMap = {}

def cteateTopology(topo, remoteControllerIp, remoteControllerPort, rootCount, depth, hostCount):
	global sIndex
	global hIndex
	global hostMap
	global linkMap
	global switchMap
	hostMap = {}
	linkMap = {}
	switchMap = {}
	sIndex=1
	hIndex=1
	# connects topology to sdn controller 
	topo.addController( 'c0', controller=RemoteController, ip=remoteControllerIp, port=remoteControllerPort)
	rootSwitches = []
	for i in range(0, int(rootCount)):
		sIndexValue = 167772160 + 2560 + sIndex
		sIp = int2Ip(sIndexValue)
		s_new = topo.addSwitch('s%s' % sIndex, protocols=["OpenFlow13"], ip='%s' % sIp)
		rootSwitches.append(s_new)
		sIndex = sIndex + 1
		insertNodes(topo, s_new, depth, hostCount)
	
	rootLinks = []
	for rootSw in rootSwitches:
		for targetRootSw in rootSwitches:
			if(rootSw.name!=targetRootSw.name) and (rootSw.name+"-"+targetRootSw.name not in rootLinks):
				topo.addLink(rootSw, targetRootSw)
				rootLinks.append(rootSw.name+"-"+targetRootSw.name)
				rootLinks.append(targetRootSw.name+"-"+rootSw.name)

	for host in topo.hosts:
		hostMap[host.name] = host
	for sw in topo.switches:
		switchMap[sw.name] = sw
	for link in topo.links:link
		linkMap[link.intf1.node.name+'-'+link.intf2.node.name] = 'UP'
			
def insertNodes(topo, switch, depth, hostCount):
    global sIndex
    global hIndex
    for i in range(1, int(hostCount) + 1):
		# if depth is greater then 0 addSwitches and link them to input switch, then diminish depth count and recursively call same method 		
        if depth > 0:
                sIndexValue = 167772160 + 2560 + sIndex
                sIp = int2Ip(sIndexValue)
                s = topo.addSwitch('s%s' % sIndex, protocols=["OpenFlow13"], ip='%s' % sIp)
                sIndex += 1
                topo.addLink(s, switch)
                inDepth = depth - 1
                insertNodes(topo, s, inDepth, hostCount)
        else:
		# if depth is 0 then add hosts and link them to given switch
                for j in range(1, int(hostCount) + 2):
                        #print 'hIndex : "', hIndex
                        hIndexValue = 167772160 + hIndex # number valur of 10.0.0.0 + hIndex
						# calculates ip value of given number
                        hIp = int2Ip(hIndexValue)
                        if(hIndex<10):
                            macAddress = "00:00:00:00:00:0"+str(hIndex)
                        else:
                            macAddress = "00:00:00:00:00:"+str(hIndex)
                        h = topo.addHost('h%s' % hIndex, ip='%s' % hIp, mac=macAddress, cpu=0.5)
                        topo.addLink(h, switch)
                        hIndex += 1
                return;
				
def int2Ip(ipnum):
    o1 = int(ipnum / 16777216) % 256
    o2 = int(ipnum / 65536) % 256
    o3 = int(ipnum / 256) % 256
    o4 = int(ipnum) % 256
    return '%(o1)s.%(o2)s.%(o3)s.%(o4)s' % locals()
	
def startServices(topo, hostCount):
	hosts = topo.hosts
	for i in range(1, int(hostCount) + 1):
		if i==1 or i==7 or i==13 or i==19 or i==25 or i==31 or i==37 or i==43:
			host = hosts[i-1]
			command = "./servicerun.sh 10.0.0." + str(i) + " /scn/topo/datacpu/h" + str(i) +".cpu >log/s"+str(i)+".log &"
			host.cmd(command)
	return;

def startClients(topo, hostCount):
	hosts = topo.hosts
	for i in range(1, int(hostCount) + 1):
		if i!=1 and i!=7 and i!=13 and i!=19 and i!=25 and i!=31 and i!=37 and i!=43:
			host = hosts[i-1]
			command = "./interest.sh 10.0.0." + str(i) + " >log/i" + str(i) + ".log &"
			host.cmd(command);
	return;

if __name__ == "__main__":
	lg.setLogLevel( 'info' )
	info('Cleaning UP mininet')
	ensureRoot()
	cleanup()
	info( "*** Initializing Mininet and kernel modules\n" )
	OVSKernelSwitch.setup()
	topo = Mininet(topo=None, host=CPULimitedHost, build=False)
	cteateTopology(topo, '192.168.4.78', 6633, 4, 2, 2)
	topo.start()
	info("*** Pinging all\n")
	topo.pingAll()
	info("*** Starting services\n")
	startServices(topo,48)
	info( "*** Starting CLI\n" )
	CLI(topo)
	topo.stop()
	
