package tr.edu.boun.cmpe.scn.app.matrix.service;

import tr.edu.boun.cmpe.scn.app.matrix.common.Constants;
import tr.edu.boun.cmpe.scn.service.ScnServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by esinka on 3/22/2017.
 */
public class Run {

    private static ScnServer server;

    public static void main(String args[]) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: <Source IP> <ServicePort> <CpuResource>");
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

        String cpuResource = args[2];
        if (cpuResource == null) {
            System.out.println("CpuResource must be given!");
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        server = new ScnServer(srcIp, Integer.parseInt(servicePort), Constants.SERVICE_NAME, cpuResource, new InterestListener());
        executorService.execute(server);
    }
}
