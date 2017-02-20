package tr.edu.boun.cmpe.scn.client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

/**
 * Created by esinka on 2/4/2017.
 */
public class CyclicCounter {
    private final int rangeMin = 8000;
    private final int rangeMax = 9000;
    private final AtomicInteger counter = new AtomicInteger(rangeMin - 1);

    private static CyclicCounter instance = new CyclicCounter();

    private CyclicCounter() {
    }

    public static CyclicCounter getInstance() {
        return instance;
    }

    public int cyclicallyIncrementAndGet() {
        return counter.accumulateAndGet(1, new IntBinaryOperator() {
            @Override
            public int applyAsInt(int index, int increment) {
                return ++index > rangeMax ? rangeMin : index;
            }
        });
    }
}
