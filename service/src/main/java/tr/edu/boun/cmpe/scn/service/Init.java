package tr.edu.boun.cmpe.scn.service;

/**
 * Created by esinka on 1/28/2017.
 */
public class Init {

    private static ScnServer server;

    public static void main(String args[]) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: <Source IP> <ServicePort> <ServiceName> <CpuResource>");
            return;
        }
        String srcIp = args[0];
        if (srcIp == null) {
            System.out.println("Source IP must be given!");
            return;
        }

        String servicePort = args[1];
        if (servicePort == null) {
            System.out.println("ServicePort must be given!");
            return;
        }

        String serviceName = args[2];
        if (serviceName == null) {
            System.out.println("ServiceName must be given!");
            return;
        }

        String cpuResource = args[3];
        if (cpuResource == null) {
            System.out.println("CpuResource must be given!");
            return;
        }

        server = new ScnServer(srcIp, Integer.parseInt(servicePort), serviceName, cpuResource);
        server.run();
    }
}
