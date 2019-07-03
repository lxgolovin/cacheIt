package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Mru;
import com.lxgolovin.cache.type.MemoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create usefull tests to check cache controller working
 */
class MruCacheControllerTest {

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
        Cache<Integer, Integer> cacheLevel0 = new MemoryCache<>(mruLev0);
        cc = new CacheController<>(cacheLevel0);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.cache(x,x*x));
    }

    /**
     * In the test trying to add and remove levels and check the output
     */
    @Test
    void addRemoveCacheLevels() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLev1);

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
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(7, cc.cache(4,4).getKey());
        assertEquals(4, cc.cache(5,5).getKey());
    }

    /**
     * Simple tests for get method
     */
    @Test
    void get() {
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(1,cc.get(1));
        assertEquals(4,cc.get(2));
        assertEquals(49,cc.get(7));
        assertNull(cc.get(4));
    }

    /**
     * Simple tests to check pop method
     * During this task 3 level cache created and pop is done for many cases
     */
    @Test
    void popFor3LevelCacheUntilRemoveAll() {
        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLev1);
        CacheAlgorithm<Integer> mruLev2 = new Mru<>();
        Cache<Integer, Integer> cacheLevel2 = new MemoryCache<>(mruLev2);
        assertEquals(2,cc.addLevel(cacheLevel1));
        assertEquals(3,cc.addLevel(cacheLevel2));

        // now got: level 0: {0->0, 1->1, 2->4, 3->9, 7->49}; level1: {}; level2" {}
        // fill in levels:
        IntStream.rangeClosed(10,18).forEach(x -> cc.cache(x,x*x));
        // now got: level0: {0,1,2,3,18}; level1: {7,10,11,12,17}; level2: {13,14,15,16}
        // if use pop MRU level0 "18" goes to level1, "17" goes to level2. Return should be 17->null
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        assertNull(cc.pop());
        // size should not change. It will be 14
        assertEquals(14,cc.size());
        // get for "17" with still return 289!!! But now from level 2
        // now got: level0: {0,1,2,3}; level1: {7,10,11,12,18}; level2: {13,14,15,16,17}
        assertEquals(289, cc.get(17));

        // same for other
        assertEquals(17, cc.pop().getKey()); // 17->289 removed
        assertEquals(18, cc.pop().getKey()); // 18->324 removed
        assertEquals(3, cc.pop().getKey()); // 3->9 removed
        assertEquals(2, cc.pop().getKey()); // 2->4 removed
        // now got: level0: {}; level1: {7,10,11,12,0}; level2: {13,14,15,16,1} and size 10
        assertEquals(10,cc.size());
        // now pop to check how it works with empty level0
        assertEquals(1, cc.pop().getKey()); // 1->1 removed

        // remove all:
        cc.clear();
        assertEquals(0,cc.size());
        // pop for empty should be null
        assertNull(cc.pop());
    }

    /**
     * Simple tests to check delete method
     */
    @Test
    void deleteFor2LevelCacheUntilRemoveAll() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {0,1,2,3,7}; level 1: {}.
        assertNull(cc.cache(8,64));
        // now: level 0: {0,1,2,3,8}; level 1: {7}.

        assertEquals(1, cc.delete(1));
        assertEquals(4, cc.delete(2));
        assertEquals(9, cc.delete(3));
        // now: level 0: {0,8}; level 1: {7}.
        assertEquals(3,cc.size());
        assertEquals(10,cc.sizeMax());
        // try to delete 3 one more time
        assertNull(cc.delete(3));

        // now: level 0: {0,8}; level 1: {7}.
        assertEquals(0, cc.delete(0));
        assertEquals(64, cc.delete(8));

        // now: level 0: {}; level 1: {7}.
        // change 7->49 to 7->14. Size will be 1 and max size 10 (2 levels)
        assertEquals(49,cc.cache(7,14).getValue());
        assertEquals(14, cc.get(7));
        assertEquals(1,cc.size());
        assertEquals(10,cc.sizeMax());

        // delete last element
        assertEquals(14, cc.delete(7));
        assertNull(cc.get(7));
    }

    /**
     * Simple tests to get current size of the cache and maximum available size of cache
     */
    @Test
    void sizeAndMaxSize() {
        CacheAlgorithm<Integer> mruLev1 = new Mru<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(5,cc.size());
        assertEquals(5,cc.sizeMax());
        // add level. current size not changed, max size doubled
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level 0: {0,1,2,3,7}; level 1: {}.
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {0,1,2,3,7}; level 1: {}. adding one value (actually renew). Nothing is changed in sizes
        assertEquals(2, cc.cache(2,8).getKey());
        assertEquals(5,cc.size());
        assertEquals(10,cc.sizeMax());

        // now: level 0: {0,1,3,7,2}; level 1: {}; adding one new key-value. Current size increase
        assertNull(cc.cache(4,16));
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
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLev1);

        // Stream starts with {0..7} cached in level 0: {0->0, 1->1, 2->4, 3->9, 7->49}
        assertEquals(49, cc.cache(7,7).getValue());
        // now: level0 {0->0, 1->1, 2->4, 3->9, 7->7}
        assertEquals(7, cc.cache(8,64).getKey());
        // now: level0 {0->0, 1->1, 2->4, 3->9, 8->64}
        assertTrue(cc.isLevelFull(0));
        assertEquals(1, cc.levels());
        assertEquals(2,cc.addLevel(cacheLevel1));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 8->64}; level1 {}
        assertNull(cc.cache(9,81));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->64}
        assertEquals(64, cc.get(8));
        // if adding to cache 8->8 again mapping 8->64 should be deleted. Check this out
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleEntry<>(8, 64);
        assertEquals(testEntry, cc.cache(8, 8));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->8}
        assertEquals(8, cc.get(8));

        // now: level0 {0->0, 1->1, 2->4, 3->9, 9->81}; level1 {8->8}
        // if adding 5->25, return value should be null!!
        assertNull(cc.cache(5,25));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 5->25}; level1 {8->8, 9->81}

        // add keys-values to fill level1 totally
        assertNull(cc.cache(6,36));
        assertNull(cc.cache(7,49));
        assertNull(cc.cache(4,8));
        // now: level0 {0->0, 1->1, 2->4, 3->9, 4->8}; level1 {8->8, 9->81, 5->25, 6->36, 7->49}
        assertTrue(cc.isLevelFull(1));

        // one more 11->121, it pops out 7->49 from level1!!
        assertEquals(7, cc.cache(11,121).getKey());
        // if getting key that is not present in cache, got null
        assertNull(cc.get(1000));
    }
}

