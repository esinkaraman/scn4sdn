package tr.edu.boun.cmpe.scn.probelog;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by esinka on 4/4/2017.
 */
public class ProbeLogger {
    private static final Logger log = getLogger(ProbeLogger.class);

    public static void logCpuUsage(long cpuUsage, String mac) {
        log.info("mac={};cpuUsage={}", mac,cpuUsage);
    }
}
