package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class CacheControllerTest {

    /**
     *
     */
    private CacheController<Integer, Integer> cc;

    /**
     *
     */
    private Cache<Integer, Integer> lruCache;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        cc = new CacheController<>();

        CacheAlgorithm<Integer> lru = new LruMru<>();
        lruCache = new MemoryCache<>(lru);
    }

    /**
     *
     */
    @Test
    void addRemoveCacheLevels() {
        assertEquals(2,cc.addLevel(lruCache));
        cc.removeLevel(1);
        assertEquals(1, cc.levels());
        cc.removeLevel(0);
        assertEquals(0, cc.levels());
        assertThrows(NoSuchElementException.class,
                () -> cc.load(1,1));
    }

    /**
     *
     */
    @Test
    void numberOfLevels() {
        assertEquals(1, cc.levels());
    }

    /**
     *
     */
    @Test
    void loadDataLruAlgorithm() {
        assertEquals(1, cc.load(1,1));
        assertEquals(2, cc.load(2,1));
        assertEquals(3, cc.load(3,1));
        assertEquals(4, cc.load(4,1));
        assertEquals(5, cc.load(5,1));
        assertEquals(1,cc.load(6,1));
    }
}