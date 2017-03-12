package tr.edu.boun.cmpe.scn;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by esinka on 3/1/2017.
 */
public class DateUtils {

    public static Date dateAddMilliSeconds(int milliseconds) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, milliseconds);
        return cal.getTime();
    }

    public static long currentTimeAddMilliSeconds(int milliseconds) {
        return System.currentTimeMillis() + milliseconds;
    }
}
