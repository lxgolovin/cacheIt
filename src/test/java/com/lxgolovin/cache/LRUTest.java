package com.lxgolovin.cache;

import com.lxgolovin.cache.LRU;
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
        Arrays.stream(arr).forEach(x -> lru.add(x));
    }


    @Test
    void add() {
        //TODO
    }

    @Test
    void renew() {
        //TODO
    }

    @Test
    void head() {
        assertEquals(1,lru.head());
        lru.renew(1);
        assertEquals(2,lru.head());
        lru.delete(2);
        assertEquals(3,lru.head());
        lru.delete(4);
        assertEquals(3,lru.head());
        lru.renew(3);
        assertEquals(5,lru.head());
    }

    @Test
    void tail() {
        assertEquals(8, lru.tail());
        lru.renew(4);
        assertEquals(4, lru.tail());
        assertThrows(IllegalArgumentException.class,
                () -> { lru.delete(10); }  );
    }

    @Test
    void delete() {
        assertEquals(2,lru.delete(2));
        assertEquals(1,lru.head());
        assertEquals(1,lru.delete(1));
        assertEquals(3,lru.head());
        assertThrows(IllegalArgumentException.class,
                () -> { lru.delete(1); }  );
    }

    @AfterEach
    void tearDown() {
    }

}