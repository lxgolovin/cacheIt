package com.lxgolovin.cache;

// TODO: To be documented
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class LruAlgorithmTest {

    /**
     *
     */
    private CacheAlgorithm<Integer> lQueue;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        lQueue = new LruAlgorithm<>();
        IntStream.rangeClosed(1,4).forEach(x -> lQueue.shift(x));
    }

    /**
     *
     */
    @Test
    void getTypeDefault() {
        assertEquals(LruAlgorithm.DEFAULT_ALGORITHM_TYPE, lQueue.getType());
        assertEquals(LruAlgorithm.LRU_ALGORITHM, lQueue.toString());
    }

    /**
     *
     */
    @Test
    void lruAlgorithm() {
        // {1,2,3,4} after initialization phase
        assertEquals(1,lQueue.delete());
        // {2,3,4}
        lQueue.shift(1);
        //{2,3,4,1}
        assertEquals(2,lQueue.delete(2));
        // {3,4,1}
        lQueue.shift(3);
        // {4,1,3}
        assertEquals(4,lQueue.delete());
        assertThrows(IllegalArgumentException.class,
                () -> lQueue.shift(null));
    }
    /**
     *
     */
    @Test
    void deleteAll() {
        lQueue.flash();
        assertThrows(IllegalArgumentException.class,
                () -> lQueue.delete());
    }
}