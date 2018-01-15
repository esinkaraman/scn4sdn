package tr.edu.boun.cmpe.scn.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.onlab.osgi.DefaultServiceDirectory;
import tr.edu.boun.cmpe.scn.api.common.ScnService;
import tr.edu.boun.cmpe.scn.api.common.ServiceInfo;

/**
 * Created by esinka on 1/6/2017.
 */
@Command(scope = "scncli", name = "scn",
        description = "Lists all service instances")
public class ServiceListCommand extends OsgiCommandSupport {

    private static final String FMT = "ServiceName=%s, SwitchId=%s SwitchPort=%s ServicePort=%s LastActivity=%s %s";

    /**
     * Executes this command.
     */
    @Override
    protected Object doExecute() {
        Iterable<ServiceInfo> serviceInstances = DefaultServiceDirectory.getService(ScnService.class).getServiceInstances();
        serviceInstances.forEach(serviceInfo -> {
            System.out.println(serviceInfo.toString());
            System.out.println("---------------------------");
        });
        return null;
    }
}
