package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Mru;
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
class CacheControllerMruTest {

    /**
     * Cache controller
     */
    private CacheController<Integer, Integer> cc;

    /**
     * Cache algorithm for level 0
     */
    private final CacheAlgorithm<Integer> mruLev0 = new Mru<>();

    /**
     * Init CacheController with MRU algorithm with one level
     * MRU cache is initialized
     * only one level number 0 {0->0, 1->1, 2->4, 3->9, 7->49}
     */
    @BeforeEach
    void setUp() {
        int minRange = 0;
        int maxRange = 7;
        Cache<Integer, Integer> cacheLevel0 = new CacheLevel<>(mruLev0);
        cc = new CacheController<>(cacheLevel0);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.cache(x,x*x));
    }

    /**
     * In the test trying to add and remove levels and check the output
     */
    @Test
    void addRemoveCacheLevels() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);

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
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);
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
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(Optional.of(7), cc.cache(4,4).map(Map.Entry::getKey));
        assertEquals(Optional.of(4), cc.cache(5,5).map(Map.Entry::getKey));
    }

    /**
     * Simple tests for get method
     */
    @Test
    void get() {
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(Optional.of(1),cc.get(1));
        assertEquals(Optional.of(4),cc.get(2));
        assertEquals(Optional.of(49),cc.get(7));
        assertFalse(cc.get(4).isPresent());
    }

    /**
     * Simple tests to check pop method
     * During this task 3 level cache created and pop is done for many cases
     */
    @Test
    void popFor3LevelCacheUntilRemoveAll() {
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);
        CacheAlgorithm<Integer> mruLev2 = new Mru<>();
        Cache<Integer, Integer> cacheLevel2 = new CacheLevel<>(mruLev2);
        assertEquals(2,cc.addLevel(cacheLevel1));
        assertEquals(3,cc.addLevel(cacheLevel2));

        // now got: level 0: {0->0, 1->1, 2->4, 3->9, 7->49}; level1: {}; level2" {}
        // fill in levels:
        IntStream.rangeClosed(10,18).forEach(x -> cc.cache(x,x*x));
        // now got: level0: {0,1,2,3,18}; level1: {7,10,11,12,17}; level2: {13,14,15,16}
        // if use pop MRU level0 "18" goes to level1, "17" goes to level2. Return should be 17->null
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        assertFalse(cc.pop().isPresent());
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        // get for "17" with still return 289!!! But now from level 2
        // now got: level0: {0,1,2,3}; level1: {7,10,11,12,18}; level2: {13,14,15,16,17}
        assertEquals(Optional.of(289), cc.get(17));
        // after "get" element 7-289 moved to level0
        // now got: level0: {0,1,2,3,17}; level1: {7,10,11,12,18}; level2: {13,14,15,16}
        assertFalse(cc.pop().isPresent());
        // now got: level0: {0,1,2,3}; level1: {7,10,11,12,17}; level2: {13,14,15,16,18}
        assertEquals(Optional.of(18), cc.pop().map(Map.Entry::getKey)); // 18->324 removed
        assertEquals(Optional.of(17), cc.pop().map(Map.Entry::getKey)); // 17->289 removed
        assertEquals(Optional.of(3), cc.pop().map(Map.Entry::getKey)); // 3->9 removed
        assertEquals(Optional.of(2), cc.pop().map(Map.Entry::getKey)); // 2->4 removed
        // now got: level0: {}; level1: {7,10,11,12,0}; level2: {13,14,15,16,1} and size 10
        assertEquals(10,cc.size());
        // now pop to check how it works with empty level0
        assertEquals(Optional.of(1), cc.pop().map(Map.Entry::getKey)); // 1->1 removed

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
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {0,1,2,3,7}; level 1: {}.
        assertFalse(cc.cache(8,64).isPresent());
        // now: level 0: {0,1,2,3,8}; level 1: {7}.

        assertEquals(Optional.of(1), cc.delete(1));
        assertEquals(Optional.of(4), cc.delete(2));
        assertEquals(Optional.of(9), cc.delete(3));
        // now: level 0: {0,8}; level 1: {7}.
        assertEquals(3,cc.size());
        assertEquals(10,cc.sizeMax());
        // try to delete 3 one more time
        assertFalse(cc.delete(3).isPresent());

        // now: level 0: {0,8}; level 1: {7}.
        assertEquals(Optional.of(0), cc.delete(0));
        assertEquals(Optional.of(64), cc.delete(8));

        // now: level 0: {}; level 1: {7}.
        // change 7->49 to 7->14. Size will be 1 and max size 10 (2 levels)
        assertEquals(Optional.of(49),cc.cache(7,14).map(Map.Entry::getValue));
        assertEquals(Optional.of(14), cc.get(7));
        assertEquals(1,cc.size());
        assertEquals(10,cc.sizeMax());

        // delete last element
        assertEquals(Optional.of(14), cc.delete(7));
        assertFalse(cc.get(7).isPresent());
    }

    /**
     * Simple tests to get current size of the cache and maximum available size of cache
     */
    @Test
    void sizeAndMaxSize() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(5,cc.size());
        assertEquals(5,cc.sizeMax());
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {0,1,2,3,7}; level 1: {}.
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {0,1,2,3,7}; level 1: {}. adding one value (actually renew). Nothing is changed in sizes
        assertEquals(Optional.of(2), cc.cache(2,8).map(Map.Entry::getKey));
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {0,1,3,7,2}; level 1: {}; adding one new key-value. Current size increase
        assertFalse(cc.cache(4,16).isPresent());
        // now: level 0: {0,1,3,7,4}; level 1: {2};
        assertEquals(6,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {0,1,3,7,4}; level 1: {2}; check size after deleting level 1
        assertEquals(1, cc.removeLevel(0));
        // now: level 0: {2};
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
     * Checking main MRU cache algorithm to work with 2 levels
     * loading, moving and deleting (only popping, not delete by key)
     * data between 2 levels
     */
    @Test
    void loadDataAdding2Levels() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(Optional.of(49), cc.cache(7,7).map(Map.Entry::getValue));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 7->7}
        assertEquals(Optional.of(7), cc.cache(8,64).map(Map.Entry::getKey));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 8->64}
        assertTrue(cc.isLevelFull(0));
        assertEquals(1, cc.levels());
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 8->64}; level1 {}
        assertFalse(cc.cache(9,81).isPresent());
        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->64}
        assertEquals(Optional.of(64), cc.get(8));
        // if adding to cache 8->8 again mapping 8->64 should be deleted. Check this out
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleEntry<>(8, 64);
        assertEquals(testEntry, cc.cache(8, 8).orElse(null));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->8}
        assertEquals(Optional.of(8), cc.get(8));

        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->8}
        // if adding 5->25, return value should be null!!
        assertFalse(cc.cache(5,25).isPresent());
        // now: level0 {0->0, 1->1, 2->4, 3->9, 5->25}; level1 {8->8, 9->81}

        // add keys-values to fill level1 totally
        assertFalse(cc.cache(6,36).isPresent());
        assertFalse(cc.cache(7,49).isPresent());
        assertFalse(cc.cache(4,8).isPresent());
        // now: level0 {0->0, 1->1, 2->4, 3->9, 4->8}; level1 {8->8, 9->81, 5->25, 6->36, 7->49}
        assertTrue(cc.isLevelFull(1));

        // one more 11->121, it pops out 7->49 from level1!!
        assertEquals(Optional.of(7), cc.cache(11,121).map(Map.Entry::getKey));
        // if getting key that is not present in cache, got null
        assertFalse(cc.get(1000).isPresent());
    }
}

