package tr.edu.boun.cmpe.scn.api.common;

/**
 * Created by esinka on 2/4/2017.
 */
public class Tool {

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
}
