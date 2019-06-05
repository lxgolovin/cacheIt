package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
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
     * Init CacheController with LRU algorithm with one level
     */
    @BeforeEach
    void setUp() {
        int minRange = 0;
        int maxRange = 7;

        CacheAlgorithm<Integer> mruLevel1 = new MruAlgorithm<>();
        Cache<Integer, Integer> cacheLevel1 = new MemoryCache<>(mruLevel1);
        cc = new CacheController<>(cacheLevel1);

        IntStream.rangeClosed(minRange,maxRange).forEach(x -> cc.load(x,x*x));
    }

    /**
     *
     */
    @Test
    void loadDataAdding2Levels() {
        CacheAlgorithm<Integer> mruLevel2 = new MruAlgorithm<>();
        Cache<Integer, Integer> cacheLevel2 = new MemoryCache<>(mruLevel2);

        // Stream starts with {0..7} cached in level 1: {0,1,2,3,7}
        assertEquals(7, cc.load(8,64).getKey());
        // level1 {0,1,2,3,8}
        assertTrue(cc.isLevelFull(0));
        assertEquals(2,cc.addLevel(cacheLevel2));
        // level1 {0,1,2,3,8}
        // level2 {}
        assertEquals(8, cc.load(9,81).getKey());
        // level1 {0,1,2,3,9}
        // level2 {8}
        assertEquals(1, cc.load(new AbstractMap.SimpleEntry<>(1,14)).getKey());
        // level1 {0,2,3,9,1}
        // level2 {8}
        assertEquals(0, cc.get(0,0).getKey());
        assertEquals(2, cc.get(2,0).getKey());
        assertEquals(3, cc.get(3,0).getKey());
        assertEquals(9, cc.get(9,0).getKey());
        assertEquals(1, cc.get(1,0).getKey());
        // level1 {0,2,3,9,1}
        // level2 {8}
        assertEquals(8, cc.get(8,1).getKey());
        assertEquals(1, cc.load(10,10).getKey());
        // Now we have level1 {0,2,3,9,10} and level2 {8,1}
        assertFalse(cc.isLevelFull(1));

        assertEquals(10, cc.load(11,10).getKey());
        assertEquals(11, cc.load(12,10).getKey());
        assertEquals(12, cc.load(13,10).getKey());
        // Now we have level1 {0,2,3,9,13} and level2 {8,1,10,11,12}
        assertEquals(12, cc.load(14,10).getKey());
        // Now we have level1 {0,2,3,9,14} and level2 {8,1,10,11,13}
        assertEquals(13, cc.load(15,10).getKey());
        // Now we have level1 {0,2,3,9,15} and level2 {8,1,10,11,14}
        assertEquals(1, cc.getLevel(14));
        assertEquals(0, cc.getLevel(15));
        assertEquals(1, cc.getIter(1).getKey());
        assertTrue(cc.isLevelFull(0));

    }
}