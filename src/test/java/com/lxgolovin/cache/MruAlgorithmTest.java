package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Create checks-tests to get if MRU algorithm based on interface {@link CacheAlgorithm}
 * with implementation {@link AbstractRuAlgorithm} and {@link MruAlgorithm} is working
 * @see CacheAlgorithm
 * @see AbstractRuAlgorithm
 * @see MruAlgorithm
 */
class MruAlgorithmTest {

    /**
     * defining algorithm queue to keep values
     */
    private final CacheAlgorithm<Integer> mQueue = new MruAlgorithm<>();

    /**
     * Init queue with MRU algorithm and start queue with first 4 values to check
     */
    @BeforeEach
    void setUp() {
        IntStream.rangeClosed(1,4).forEach(mQueue::shift);
    }

    /**
     * Checks getType and toString methods
     */
    @Test
    void getTypeMRU() {
        assertEquals(MruAlgorithm.MRU_ALGORITHM, mQueue.getType());
        assertEquals(MruAlgorithm.MRU_ALGORITHM, mQueue.toString());
    }

    /**
     * checks if algorithm is working
     * Here is the main logic of MRU algorithm
     */
    @Test
    void deleteCandadatesAfterShiftsMru() {
        // {1,2,3,4} after initialization phase
        assertEquals(4, mQueue.pop());
        // {1,2,3}
        mQueue.shift(4);
        // {1,2,3,4}
        mQueue.delete(4);
        // {1,2,3}
        assertEquals(3, mQueue.pop());
        // {1,2}
        mQueue.shift(1);
        // {2,1}
        assertEquals(1, mQueue.pop());
        // {2}
        assertEquals(2, mQueue.pop());
        assertNull(mQueue.pop());
    }

    /**
     * Test implementation of shift method in MRU algorithm
     */
    @Test
    void shiftMethodLru() {
        // {1,2,3,4} after initialization phase
        mQueue.shift(1);
        // {2,3,4,1}
        mQueue.shift(5);
        // {2,3,4,1,5}
        mQueue.shift(6);
        // {2,3,4,1,5,6}
        mQueue.shift(7);
        // {2,3,4,1,5,6,7}. if use pop, should delete 7 in MRU algorithm. Check this out:
        assertEquals(7,mQueue.pop());
        // check null as input
        assertThrows(IllegalArgumentException.class,
                () -> mQueue.shift(null));
    }

    /**
     * Test implementation of delete method in MRU algorithm
     */
    @Test
    void delete() {
        // {1,2,3,4} after initialization phase
        mQueue.delete(1);
        mQueue.delete(2);
        // {3,4} 4 - should be deleted by pop. Check it:
        assertEquals(4,mQueue.pop());
        mQueue.delete(3);
        // {}. But if we try to delete more, nothing happens
        assertNull(mQueue.pop());
        mQueue.delete(5);
        // check null as input
        assertThrows(IllegalArgumentException.class,
                () -> mQueue.delete(null));
    }

    /**
     * Clear queue
     */
    @Test
    void deleteAll() {
        mQueue.clear();
        assertNull(mQueue.pop());
    }
}