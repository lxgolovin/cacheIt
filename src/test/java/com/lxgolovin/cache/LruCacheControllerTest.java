package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create usefull tests to check cache controller working
 */
class LruCacheControllerTest {

    /**
     * Cache controller
     */
    private CacheController<Integer, Integer> cc;

    /**
     * Cache algorithm
     */
    private CacheAlgorithm<Integer> lru = new LruAlgorithm<>();

    /**
     * Cache type
     */
    private Cache<Integer, Integer> cache;

    /**
     * Init CacheController with LRU algorithm with one level
     */
    @BeforeEach
    void setUp() {
        int minRange = 0;
        int maxRange = 7;
        Cache<Integer, Integer> initCacheLevel = new MemoryCache<>(lru);
        cc = new CacheController<>(initCacheLevel);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.load(x,x*x));
    }

    /**
     * In the test trying to add and remove levels and check the output
     */
    @Test
    void addRemoveCacheLevels() {
        cache = new MemoryCache<>(lru);
        assertEquals(2,cc.addLevel(cache));
        assertEquals(1, cc.removeLevel(1));
        assertEquals(0, cc.removeLevel(0));
        assertEquals(0, cc.levels());
        // attempt to load data to empty cache (no levels left)
        assertThrows(NoSuchElementException.class,
                () -> cc.load(1,1));
    }

    /**
     * Get number of levels
     */
    @Test
    void numberOfLevels() {
        assertEquals(1, cc.levels());
    }

    /**
     *
     */
    @Test
    void loadDataLruAlgorithmAdding2Levels() {
        cache = new MemoryCache<>(lru);
        // Stream starts with {0..7} cached in level 1: {3,4,5,6,7}
        assertEquals(3, cc.load(8,64).getKey());
        // level1 {4,5,6,7,8}
        assertTrue(cc.isLevelFull(0));
        assertEquals(1, cc.levels());
        assertEquals(2,cc.addLevel(cache));
        // level1 {4,5,6,7,8}
        // level2 {}
        assertEquals(4, cc.load(9,81).getKey());
        // level1 {5,6,7,8,9}
        // level2 {4}
        assertEquals(5, cc.load(new AbstractMap.SimpleEntry<>(1,1)).getKey());
        // level1 {6,7,8,9,1}
        // level2 {5,4}
        assertEquals(6, cc.get(6,0).getKey());
        assertEquals(7, cc.get(7,0).getKey());
        assertEquals(8, cc.get(8,0).getKey());
        assertEquals(9, cc.get(9,0).getKey());
        assertEquals(1, cc.get(1,0).getKey());
        // level1 {6,7,8,9,1}
        // level2 {5,4}
        assertEquals(4, cc.get(4,1).getKey());
        assertEquals(5, cc.get(5,1).getKey());
        assertTrue(cc.isLevelFull(0));
        assertFalse(cc.isLevelFull(1));

    }
}