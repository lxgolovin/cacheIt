package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LruMruTest {
    private CacheAlgorithm<Integer> lqueue;
    private CacheAlgorithm<Integer> mqueue;
    private int[] arr;

    @BeforeEach
    void setUp() {
        lqueue = new LruMru<>();
        mqueue = new LruMru<>("MRU");
        arr = new int[]{1,2,3,4};
        Arrays.stream(arr).forEach(x -> lqueue.shift(x));
        Arrays.stream(arr).forEach(x -> mqueue.shift(x));

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
        assertEquals(2,lqueue.unshift(2));
        lqueue.shift(3);
        assertEquals(4,lqueue.delete());
        assertThrows(IllegalArgumentException.class,
                () -> { lqueue.shift(null); }  );
    }

    @Test
    void mruAlgorithm() {
        assertEquals(4, mqueue.delete());
        mqueue.shift(4);
        assertEquals(4, mqueue.unshift(4));
        assertEquals(3, mqueue.delete());
        mqueue.shift(1);
        assertEquals(1, mqueue.delete());

        assertThrows(IllegalArgumentException.class,
                () -> { mqueue.shift(null); }  );

        assertEquals(2, mqueue.delete());
        assertNull(mqueue.delete());
    }

    @Test
    void deleteAll() {
        // Arrays.stream(arr).forEach(x -> lqueue.delete());
        lqueue.flash();
        assertNull(lqueue.delete());
    }
}