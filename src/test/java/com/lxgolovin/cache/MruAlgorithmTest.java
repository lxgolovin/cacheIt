package com.lxgolovin.cache;

// TODO: To be documented
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class MruAlgorithmTest {

    /**
     *
     */
    private CacheAlgorithm<Integer> mQueue;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        mQueue = new MruAlgorithm<>();
        IntStream.rangeClosed(1,4).forEach(x -> mQueue.shift(x));

    }

    /**
     *
     */
    @Test
    void getTypeMRU() {
        assertEquals(MruAlgorithm.MRU_ALGORITHM, mQueue.getType());
        assertEquals(MruAlgorithm.MRU_ALGORITHM, mQueue.toString());
    }

    /**
     *
     */
    @Test
    void mruAlgorithm() {
        // {1,2,3,4} after initialization phase
        assertEquals(4, mQueue.delete());
        // {1,2,3}
        mQueue.shift(4);
        // {1,2,3,4}
        assertEquals(4, mQueue.delete(4));
        // {1,2,3}
        assertEquals(3, mQueue.delete());
        // {1,2}
        mQueue.shift(1);
        // {2,1}
        assertEquals(1, mQueue.delete());
        // {2}
        assertThrows(IllegalArgumentException.class,
                () -> mQueue.shift(null) );
        assertEquals(2, mQueue.delete());
        // {}
        assertThrows(IllegalArgumentException.class,
                () -> mQueue.delete());
    }

    /**
     *
     */
    @Test
    void deleteAll() {
        mQueue.flash();
        assertThrows(IllegalArgumentException.class,
                () -> mQueue.delete());
    }
}