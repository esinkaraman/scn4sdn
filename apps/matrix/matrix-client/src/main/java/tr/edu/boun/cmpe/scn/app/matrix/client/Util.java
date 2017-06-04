package tr.edu.boun.cmpe.scn.app.matrix.client;

/**
 * Created by esinka on 4/4/2017.
 */
public class Util {
    public static boolean isNumeric(String str) {
        return str.chars().allMatch(Character::isDigit);
    }
}
