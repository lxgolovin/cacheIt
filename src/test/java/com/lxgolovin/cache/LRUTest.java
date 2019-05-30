package com.lxgolovin.cache;

import com.lxgolovin.cache.LRU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LRUTest {
    private LRU<Integer> lru;
    private int[] arr;

    @BeforeEach
    void setUp() {
        lru = new LRU<>();
        arr = new int[]{1,2,3,4,5,6,7,8};
        Arrays.stream(arr).forEach(x -> lru.add(x));
    }

    @Test
    void head() {
        lru.delete(1);
        assertEquals(2,lru.head());
    }

    @Test
    void tail() {
        lru.renew(4);
        assertEquals(4, lru.tail());
    }
}