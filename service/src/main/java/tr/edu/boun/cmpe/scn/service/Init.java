package tr.edu.boun.cmpe.scn.service;

/**
 * Created by esinka on 1/28/2017.
 */
public class Init {

    private static ScnServer server;

    public static void main(String args[]) throws Exception {
        if(args.length<3) {
            System.out.println("Usage: <Source IP> <ServicePort> <ServiceName>");
            return;
        }
        String srcIp = args[0];
        if(srcIp == null) {
            System.out.println("Source IP must be given!");
            return;
        }

        String servicePort = args[1];
        if(servicePort == null) {
            System.out.println("ServicePort must be given!");
            return;
        }

        String serviceName = args[2];
        if(serviceName == null) {
            System.out.println("ServiceName must be given!");
            return;
        }

        server = new ScnServer(srcIp, Integer.parseInt(servicePort), serviceName);
        server.run();
    }
}
