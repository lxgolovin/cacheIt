package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create checks-tests to get if LRU algorithm based on interface {@link CacheAlgorithm}
 * with implementation {@link AbstractRuAlgorithm} and {@link LruAlgorithm} is working
 * @see CacheAlgorithm
 * @see AbstractRuAlgorithm
 * @see LruAlgorithm
 */
class LruAlgorithmTest {

    /**
     * defining algorithm queue to keep values
     */
    private final CacheAlgorithm<Integer> lQueue = new LruAlgorithm<>();

    /**
     * Init queue with LRU algorithm and start queue with first 4 values to check
     */
    @BeforeEach
    void setUp() {
        IntStream.rangeClosed(1,4).forEach(x -> lQueue.shift(x));
    }

    /**
     * Checks getType and toString methods
     */
    @Test
    void getTypeDefault() {
        assertEquals(AbstractRuAlgorithm.DEFAULT_ALGORITHM_TYPE, lQueue.getType());
        assertEquals(AbstractRuAlgorithm.LRU_ALGORITHM, lQueue.toString());
    }

    /**
     * checks if algorithm is working
     * Here is the main logic of LRU algorithm
     */
    @Test
    void deleteCandadatesAfterShiftsLru() {
        // {1,2,3,4} after initialization phase
        assertEquals(1,lQueue.pop());
        // {2,3,4}
        lQueue.shift(1);
        //{2,3,4,1}
        lQueue.delete(2);
        // {3,4,1}
        lQueue.shift(3);
        // {4,1,3}
        assertEquals(4,lQueue.pop());
        lQueue.delete(10);

    }

    /**
     * Test implementation of shift method in LRU algorithm
     */
    @Test
    void shiftMethodLru() {
        // {1,2,3,4} after initialization phase
        lQueue.shift(1);
        // {2,3,4,1}
        lQueue.shift(5);
        // {2,3,4,1,5}
        lQueue.shift(6);
        // {2,3,4,1,5,6}
        lQueue.shift(7);
        // {2,3,4,1,5,6,7}. if use pop, should delete 2 in LRU algorithm. Check this out:
        assertEquals(2,lQueue.pop());
        // check null as input
        assertThrows(IllegalArgumentException.class,
                () -> lQueue.shift(null));
    }

    /**
     * Test implementation of delete method in LRU algorithm
     */
    @Test
    void delete() {
        // {1,2,3,4} after initialization phase
        lQueue.delete(1);
        lQueue.delete(2);
        // {3,4} 3 - should be deleted by pop. Check it:
        assertEquals(3,lQueue.pop());
        lQueue.delete(4);
        // {}. But if we try to delete more, nothing happens
        lQueue.delete(5);
        // check null as input
        assertThrows(IllegalArgumentException.class,
                () -> lQueue.delete(null));
    }
    //! add methods to test delete() and shift() separately

    /**
     * Clear queue
     */
    @Test
    void deleteAll() {
        lQueue.clearAll();
        assertNull(lQueue.pop());
    }
}