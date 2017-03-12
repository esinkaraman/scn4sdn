package tr.edu.boun.cmpe.scn.client;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Created by esinka on 2/25/2017.
 */
public class ManagementBean {

    public static double getUsage() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        return operatingSystemMXBean.getSystemLoadAverage();
    }

    public static void main(String[] args) {
        System.out.println(getUsage());
    }
}

