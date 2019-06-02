package com.lxgolovin.cache;

// TODO: To be documented
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class LruMruTest {

    /**
     *
     */
    private CacheAlgorithm<Integer> lQueue;

    /**
     *
     */
    private CacheAlgorithm<Integer> mQueue;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        lQueue = new LruMru<>(LruMru.LRU_ALGORITHM);
        mQueue = new LruMru<>(LruMru.MRU_ALGORITHM);
        IntStream.rangeClosed(1,4).forEach(x -> lQueue.shift(x));
        IntStream.rangeClosed(1,4).forEach(x -> mQueue.shift(x));

    }

    /**
     *
     */
    @Test
    void getTypeDefault() {
        assertEquals(LruMru.DEFAULT_ALGORITHM_TYPE, lQueue.getType());
    }

    /**
     *
     */
    @Test
    void getTypeMRU() {
        assertEquals(LruMru.MRU_ALGORITHM, mQueue.getType());
    }

    /**
     *
     */
    @Test
    void lruAlgorithm() {
        assertEquals(1,lQueue.delete());
        lQueue.shift(1);
        assertEquals(2,lQueue.delete(2));
        lQueue.shift(3);
        assertEquals(4,lQueue.delete());
        assertThrows(IllegalArgumentException.class,
                () -> lQueue.shift(null) );
    }

    /**
     *
     */
    @Test
    void mruAlgorithm() {
        assertEquals(4, mQueue.delete());
        mQueue.shift(4);
        assertEquals(4, mQueue.delete(4));
        assertEquals(3, mQueue.delete());
        mQueue.shift(1);
        assertEquals(1, mQueue.delete());

        assertThrows(IllegalArgumentException.class,
                () -> mQueue.shift(null) );

        assertEquals(2, mQueue.delete());
        assertNull(mQueue.delete());
    }

    /**
     *
     */
    @Test
    void deleteAll() {
        lQueue.flash();
        assertNull(lQueue.delete());
    }
}