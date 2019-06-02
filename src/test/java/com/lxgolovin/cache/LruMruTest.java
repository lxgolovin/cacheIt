package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LruMruTest {
    private CacheAlgorithm<Integer> lqueue;
    private CacheAlgorithm<Integer> mqueue;

    @BeforeEach
    void setUp() {
        lqueue = new LruMru<>();
        mqueue = new LruMru<>("MRU");
        IntStream.rangeClosed(1,4).forEach(x -> lqueue.shift(x));
        IntStream.rangeClosed(1,4).forEach(x -> mqueue.shift(x));

    }

    @Test
    void getTypeDefault() {
        assertEquals("LRU", lqueue.getType());
    }

    @Test
    void getTypeMRU() {
        assertEquals("MRU", mqueue.getType());
    }

    @Test
    void lruAlgorithm() {
        assertEquals(1,lqueue.delete());
        lqueue.shift(1);
        assertEquals(2,lqueue.delete(2));
        lqueue.shift(3);
        assertEquals(4,lqueue.delete());
        assertThrows(IllegalArgumentException.class,
                () -> lqueue.shift(null) );
    }

    @Test
    void mruAlgorithm() {
        assertEquals(4, mqueue.delete());
        mqueue.shift(4);
        assertEquals(4, mqueue.delete(4));
        assertEquals(3, mqueue.delete());
        mqueue.shift(1);
        assertEquals(1, mqueue.delete());

        assertThrows(IllegalArgumentException.class,
                () -> mqueue.shift(null) );

        assertEquals(2, mqueue.delete());
        assertNull(mqueue.delete());
    }

    @Test
    void deleteAll() {
        lqueue.flash();
        assertNull(lqueue.delete());
    }
}