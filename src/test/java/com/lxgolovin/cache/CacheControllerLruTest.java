package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.tools.ListGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create useful tests to check cache controller working
 */
class CacheControllerLruTest {

    /**
     * Cache controller
     */
    private CacheController<Integer, Integer> cc;

    /**
     * Cache algorithm for level 0
     */
    private final CacheAlgorithm<Integer> lruLev0 = new Lru<>();

    /**
     * Init CacheController with LRU algorithm with one level
     * LRU cache is initialized
     * only one level number 0 {3->9, 4->16, 5->25, 6->36, 7->49}
     */
    @BeforeEach
    void setUp() {
        int minRange = 0;
        int maxRange = 7;
        Cache<Integer, Integer> cacheLevel0 = new CacheLevel<>(lruLev0);
        cc = new CacheController<>(cacheLevel0);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.cache(x,x*x));
    }

    /**
     * In the test trying to add and remove levels and check the output
     */
    @Test
    void addRemoveCacheLevels() {
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);

        assertEquals(2,cc.addLevel(cacheLevel1));
        assertEquals(1, cc.removeLevel(1));
        assertEquals(0, cc.removeLevel(0));
        assertEquals(0, cc.levels());

        // try to remove non existing level
        assertThrows(IndexOutOfBoundsException.class,
                () -> cc.removeLevel(5));
    }

    @Test
    void putRandomDataIntoCache() {
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);
        assertEquals(2,cc.addLevel(cacheLevel1));

        final int dataListSize = 1000;
        List<Integer> data = ListGenerator.generateInt(dataListSize);

        data.forEach(k -> {
            int v = (int) (Math.random() * dataListSize);
            cc.cache(k, v);
        });

        assertEquals(cc.size(), cc.sizeMax());
    }

    /**
     * just for testing if simple cache is working
     */
    @Test
    void cache() {
        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(Optional.of(3), cc.cache(0,0).map(Map.Entry::getKey));
        assertEquals(Optional.of(4), cc.cache(1,1).map(Map.Entry::getKey));
    }

    /**
     * Simple tests for get method
     */
    @Test
    void get() {
        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(Optional.of(16),cc.get(4));
        assertEquals(Optional.of(36),cc.get(6));
        assertEquals(Optional.of(49),cc.get(7));
        assertFalse(cc.get(1).isPresent());
    }

    /**
     * Simple tests to check pop method
     * During this task 3 level cache created and pop is done for many cases
     */
    @Test
    void popFor3LevelCacheUntilRemoveAll() {
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);
        CacheAlgorithm<Integer> lruLev2 = new Lru<>();
        Cache<Integer, Integer> cacheLevel2 = new CacheLevel<>(lruLev2);
        assertEquals(2,cc.addLevel(cacheLevel1));
        assertEquals(3,cc.addLevel(cacheLevel2));

        // now got: level0: {3-9,4-16,5-25,6-36,7-49}; level1: {}; level2" {}
        // fill in levels:
        IntStream.rangeClosed(10,18).forEach(x -> cc.cache(x,x*x));
        // now got: level0: {14,15,16,17,18}; level1: {7,10,11,12,13}; level2: {3,4,5,6}
        // if use pop LRU level0 "14" goes to level1, "7" goes to level2. Return should be 7->null
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        assertFalse(cc.pop().isPresent());
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        // get for "7" with still return 49!!! But now from level 2
        // now got: level0: {15,16,17,18}; level1: {10,11,12,13,14}; level2: {3,4,5,6,7}
        assertEquals(Optional.of(49), cc.get(7));
        // after "get" element 7-49 moved to level0
        // now got: level0: {15,16,17,18,7}; level1: {10,11,12,13,14}; level2: {3,4,5,6}
        assertFalse(cc.pop().isPresent());
        // now got: level0: {16,17,18,7}; level1: {11,12,13,14,15}; level2: {3,4,5,6,10}
        assertEquals(Optional.of(3), cc.pop().map(Map.Entry::getKey)); // 3->9 removed
        assertEquals(Optional.of(4), cc.pop().map(Map.Entry::getKey)); // 4->16 removed
        assertEquals(Optional.of(5), cc.pop().map(Map.Entry::getKey)); // 5->25 removed
        assertEquals(Optional.of(6), cc.pop().map(Map.Entry::getKey)); // 6->36 removed
        // now: level0: {}; level1: {15,16,17,18,7}; level2: {10,11,12,13,14} and size 10
        // now: level0: {}; level1: {14,15,16,17,18}; level2: {7,10,11,12,13} and size 10
        assertEquals(10,cc.size());
        // now pop to check how it works with empty level0
        assertEquals(Optional.of(10), cc.pop().map(Map.Entry::getKey)); // 7->49 removed

        // remove all:
        cc.clear();
        assertEquals(0,cc.size());
        // pop for empty should be null
        assertFalse(cc.pop().isPresent());
    }

    /**
     * Simple tests to check delete method
     */
    @Test
    void deleteFor2LevelCacheUntilRemoveAll() {
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);

        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {3,4,5,6,7}; level 1: {}.
        assertFalse(cc.cache(8,64).isPresent());
        // now: level 0: {4,5,6,7,8}; level 1: {3}.

        assertEquals(Optional.of(25), cc.delete(5));
        assertEquals(Optional.of(36), cc.delete(6));
        assertEquals(Optional.of(49), cc.delete(7));
        // now: level 0: {4,8}; level 1: {3}.
        assertEquals(3,cc.size());
        assertEquals(10,cc.sizeMax());
        // try to delete 7 one more time
        assertFalse(cc.delete(7).isPresent());

        // now: level 0: {4,8}; level 1: {3}.
        assertEquals(Optional.of(16), cc.delete(4));
        assertEquals(Optional.of(64), cc.delete(8));

        // now: level 0: {}; level 1: {3}.
        // change 3->9 to 3->21. Size will be 1 and max size 10 (2 levels)
        assertEquals(Optional.of(9),cc.cache(3,21).map(Map.Entry::getValue));
        assertEquals(Optional.of(21), cc.get(3));
        assertEquals(1,cc.size());
        assertEquals(10,cc.sizeMax());

        // delete last element
        assertEquals(Optional.of(21), cc.delete(3));
        assertFalse(cc.get(3).isPresent());
    }

    /**
     * Simple tests to get current size of the cache and maximum available size of cache
     */
    @Test
    void sizeAndMaxSize() {
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);

        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(5,cc.size());
        assertEquals(5,cc.sizeMax());
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {3,4,5,6,7}; level 1: {}.
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {3,4,5,6,7}; level 1: {}. adding one value (actually renew). Nothing is changed in sizes
        assertEquals(Optional.of(4), cc.cache(4,4).map(Map.Entry::getKey));
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {3,5,6,7,4}; level 1: {}; adding one new key-value. Current size increase
        assertFalse(cc.cache(8,64).isPresent());
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
        CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1);

        // Stream starts with {0..7} cached in level 0: {3,4,5,6,7}
        assertEquals(Optional.of(49), cc.cache(7,7).map(Map.Entry::getValue));
        // now: level0 {3,4,5,6,7} 7->7
        assertEquals(Optional.of(3), cc.cache(8,64).map(Map.Entry::getKey));
        // now: level0 {4->16, 5-25, 6->36, 7->7, 8->64}
        assertTrue(cc.isLevelFull(0));
        assertEquals(1, cc.levels());
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level0 {4->16, 5-25, 6->36, 7->7, 8->64}; level1 {}
        assertFalse(cc.cache(9,81).isPresent());
        // now: level0 {5-25, 6->36, 7->7, 8->64, 9->81}; level1 {4->16}
        assertEquals(Optional.of(16), cc.get(4));
        // now: level0 {6->36, 7->7, 8->64, 9->81, 4->16}; level1 {5-25}
        // if adding to cache 5->5 again mapping 5->25 should be deleted. Check this out
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleEntry<>(5, 25);
        assertEquals(Optional.of(testEntry), cc.cache(5, 5));
        // now: level0 {7->7, 8->64, 9->81, 4->16, 5->5}; level1 {6->36}
        assertEquals(Optional.of(5), cc.get(5));

        // now: level0 {7->7, 8->64, 9->81, 4->16, 5->5}; level1 {6->36}
        // if adding 0->0, return value should be null!!
        assertFalse(cc.cache(0,0).isPresent());
        // now: level0 {8->64, 9->81, 4->16, 5->5, 0->0}; level1 {6->36, 7->7}

        // add keys-values to fill level1 totally
        assertFalse(cc.cache(1,1).isPresent());
        assertFalse(cc.cache(2,4).isPresent());
        assertFalse(cc.cache(3,9).isPresent());
        // now: level0 {5->5, 0->0, 1->1, 2->4, 3->9}; level1 {6->36, 7->7, 8->64, 9->81, 4->16}
        assertTrue(cc.isLevelFull(1));

        // one more 11->121, it pops out 6->36 from level1!!
        assertEquals(Optional.of(6), cc.cache(11,121).map(Map.Entry::getKey));
        // if getting key that is not present in cache, got null
        assertFalse(cc.get(1000).isPresent());
    }

    @Test
    void nullChecker() {
        CacheController<Integer, Integer> cacheController = new CacheController<>(null);
        assertEquals(1, cacheController.levels());
        assertEquals(0, cacheController.size());
        assertEquals(5, cacheController.sizeMax());
        assertFalse(cacheController.contains(null));
        assertFalse(cacheController.delete(null).isPresent());
        assertFalse(cacheController.get(null).isPresent());
        assertThrows(IllegalArgumentException.class, () -> cacheController.cache(null, 0));
        assertThrows(IllegalArgumentException.class, () -> cacheController.cache(0, null));
        assertThrows(IllegalArgumentException.class, () -> cacheController.cache(null, null));

        assertThrows(IndexOutOfBoundsException.class, () -> cacheController.isLevelFull(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cacheController.removeLevel(-1));

        assertEquals(2, cacheController.addLevel(null));
        assertEquals(2, cacheController.levels());
        assertEquals(0, cacheController.size());
        assertEquals(10, cacheController.sizeMax());
    }
}