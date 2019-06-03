package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void loadData() {
        assertTrue(cc.load(1,1));
    }
}