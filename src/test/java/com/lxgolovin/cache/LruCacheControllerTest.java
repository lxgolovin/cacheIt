package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Map;
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
     * Cache algorithm for level 0
     */
    private final CacheAlgorithm<Integer> lruLev0 = new LruAlgorithm<>();

    /**
     * Init CacheController with LRU algorithm with one level
     * LRU cache is initialized
     * only one level number 0 {3->9, 4->16, 5->25, 6->36, 7->49}
     */
    @BeforeEach
    void setUp() {
        int minRange = 0;
        int maxRange = 7;
        Cache<Integer, Integer> cacheLevel0 = new MemoryCache<>(lruLev0);
        cc = new CacheController<>(cacheLevel0);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.cache(x,x*x));
    }

    /**
     * In the test trying to add and remove levels and check the output
     */
    @Test
    void addRemoveCacheLevels() {
        CacheAlgorithm<Integer> lruLev1 = new LruAlgorithm<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(lruLev1);

        assertEquals(2,cc.addLevel(cacheLevel1));
        assertEquals(1, cc.removeLevel(1));
        assertEquals(0, cc.removeLevel(0));
        assertEquals(0, cc.levels());

        // try to remove non existing level
        assertThrows(IndexOutOfBoundsException.class,
                () -> cc.removeLevel(5));
    }

    /**
     * just for testing if simple cache is working
     */
    @Test
    void cache() {
        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(3, cc.cache(0,0).getKey());
        assertEquals(4, cc.cache(1,1).getKey());
    }

    /**
     * Simple tests for get method
     */
    @Test
    void get() {
        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(16,cc.get(4));
        assertEquals(36,cc.get(6));
        assertEquals(49,cc.get(7));
    }

    /**
     * Simple tests to get current size of the cache and maximum available size of cache
     */
    @Test
    void sizeAndMaxSize() {
        CacheAlgorithm<Integer> lruLev1 = new LruAlgorithm<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(lruLev1);

        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(5,cc.size());
        assertEquals(5,cc.sizeMax());
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {3,4,5,6,7}; level 1: {}.
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {3,4,5,6,7}; level 1: {}. adding one value (actually renew). Nothing is changed in sizes
        assertEquals(4, cc.cache(4,4).getKey());
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {3,5,6,7,4}; level 1: {}; adding one new key-value. Current size increase
        assertEquals(3, cc.cache(8,64).getKey());
        // now: level 0: {5,6,7,4,8}; level 1: {3};
        assertEquals(6,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {5,6,7,4,8}; level 1: {3}; check size after deleting level 1
        assertEquals(1, cc.removeLevel(0));
        // now: level 0: {3};
        assertEquals(1,cc.size());
        assertEquals(5,cc.sizeMax());
    }

    /**
     * Get number of levels
     */
    @Test
    void numberOfLevels() {
        assertEquals(1, cc.levels());
    }

    /**
     * Checking main LRU cache algorithm to work with 2 levels
     * loading, moving and deleting (only popping, not delete by key)
     * data between 2 levels
     */
    @Test
    void loadDataAdding2Levels() {
        CacheAlgorithm<Integer> lruLev1 = new LruAlgorithm<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(lruLev1);

        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(49, cc.cache(7,7).getValue());
        // now: level0 {3,4,5,6,7} 7->7
        assertEquals(3, cc.cache(8,64).getKey());
        // now: level0 {4->16, 5-25, 6->36, 7->7, 8->64}
        assertTrue(cc.isLevelFull(0));
        assertEquals(1, cc.levels());
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level0 {4->16, 5-25, 6->36, 7->7, 8->64}; level1 {}
        assertEquals(4, cc.cache(9,81).getKey());
        // now: level0 {5-25, 6->36, 7->7, 8->64, 9->81}; level1 {4->16}
        assertEquals(16, cc.get(4));
        // if adding to cache 4->4 again mapping 4->16 should be deleted. Check this out
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleEntry<>(4, 16);
        assertEquals(testEntry, cc.cache(4, 4));
        // now: level0 {5-25, 6->36, 7->7, 8->64, 9->81}; level1 {4->4}
        assertEquals(4, cc.get(4));

        // now: level0 {5-25, 6->36, 7->7, 8->64, 9->81}; level1 {4->4}
        // if adding 0->0, return value should be null!!
        assertNull(cc.cache(0,0).getValue());
        // now: level0 {6->36, 7->7, 8->64, 9->81, 0->0}; level1 {4->4, 5->25}

        // add for keys-values to fill level1 totally
        assertEquals(6, cc.cache(1,1).getKey());
        assertEquals(7, cc.cache(2,4).getKey());
        assertEquals(8, cc.cache(3,9).getKey());
        // now: level0 {9->81, 0->0, 1->1, 2->4, 3->9}; level1 {4->4, 5-25, 6->36, 7->7, 8->64}
        assertTrue(cc.isLevelFull(1));

        // one more 11->121, it pops out 4->4 from level1!!
        assertEquals(4, cc.cache(11,121).getKey());
        // if adding key that is not present in cache, got null
        assertNull(cc.get(1000));
    }
}