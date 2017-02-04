package tr.edu.boun.cmpe.scn.api.common;

/**
 * Created by esinka on 1/28/2017.
 */
public enum ScnMessageType {
    UP(1),
    INTEREST(2),
    DATA(3),
    PROBE(4);

    private int id;

    private ScnMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ScnMessageType valueOf(int id) {
        ScnMessageType[] values = ScnMessageType.values();
        for (ScnMessageType type : values) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}
