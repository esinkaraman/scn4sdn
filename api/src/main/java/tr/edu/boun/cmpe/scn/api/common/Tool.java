package tr.edu.boun.cmpe.scn.api.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by esinka on 2/4/2017.
 */
public class Tool {

    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String ELAPSED_TIME_SEP = ";";

    public static void checkNull(Object arg, String message) {
        if (arg == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkArgument(boolean expressionValue, String message) {
        if(!expressionValue) {
            throw new IllegalArgumentException(message);
        }
    }

    public static int generateRandomInteger(int aStart, int aEnd, Random aRandom){
        if (aStart > aEnd) {
            throw new IllegalArgumentException("Start cannot exceed End.");
        }
        //get the range, casting to long to avoid overflow problems
        long range = (long)aEnd - (long)aStart + 1;
        // compute a fraction of the range, 0 <= frac < range
        long fraction = (long)(range * aRandom.nextDouble());
        return (int)(fraction + aStart);
    }

    public static int getRandomlyFromRange(int rangeStart, int rangeEndInclusive) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEndInclusive + 1);
    }

    public static String elapsedLogForClient(long elapsed, String ipAddress) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_PATTERN);
        return new StringBuilder()
                .append(sdf.format(new Date())).append(ELAPSED_TIME_SEP)
                .append(ipAddress).append(ELAPSED_TIME_SEP)
                .append(elapsed).toString();
    }

    public static String logForService(String serviceIp, String clientIp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_PATTERN);
        return new StringBuilder()
                .append(sdf.format(new Date())).append(ELAPSED_TIME_SEP)
                .append(serviceIp).append(ELAPSED_TIME_SEP)
                .append(clientIp).toString();
    }

    public static String formatTime() {
        SimpleDateFormat format = new SimpleDateFormat(TIMESTAMP_PATTERN);
        return format.format(System.currentTimeMillis());
    }
}
