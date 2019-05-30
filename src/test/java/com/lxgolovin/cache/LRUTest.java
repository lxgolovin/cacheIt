package com.lxgolovin.cache;

import org.junit.jupiter.api.AfterEach;
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
        Arrays.stream(arr).forEach( x -> lru.add(x));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void head() {
        assertEquals( 1, lru.head());
    }

    @Test
    void tail() {
        assertEquals( 4, lru.tail());
    }
}