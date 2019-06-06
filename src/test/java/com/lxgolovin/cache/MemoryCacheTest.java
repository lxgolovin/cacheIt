package com.lxgolovin.cache;

// TODO: To be documented
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class MemoryCacheTest {

    /**
     *
     */
    private Cache<Integer, Integer> lruCache;
    private Cache<Integer, Integer> mruCache;
    private CacheAlgorithm<Integer> lru;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        int maxRange = 9;
        lru = new LruAlgorithm<>();
        lruCache = new MemoryCache<>(lru);

        CacheAlgorithm<Integer> mru = new MruAlgorithm<>();
        mruCache = new MemoryCache<>(mru, 0, 0);

        IntStream.rangeClosed(1,maxRange).forEach(x->lruCache.cache(x,x));
        IntStream.rangeClosed(1,maxRange).forEach(x->mruCache.cache(x,x));
    }

    /**
     *
     */
    @Test
    void constructorWithEntry() {
        AbstractMap.SimpleEntry<Integer, String> entry = new AbstractMap.SimpleEntry<>(9,"Check entry");
        Cache<Integer, String> cache = new MemoryCache<>(lru,entry);
        assertEquals(1,cache.size());
        assertEquals(entry, cache.get(9));
    }

    /**
     *
     */
    @Test
   void getPutInLruAlgorithm() {
        // 5..9 are the elements after init and size is 5
        assertEquals(5, lruCache.get(5).getKey());
        assertEquals(9, lruCache.get(9).getKey());
        assertEquals(5, lruCache.size());
        // {6,7,8,5,9} - this is the order now
        lruCache.cache(6,8);
        assertEquals(7, lruCache.delete().getKey());
        // {8,5,9, 6->8} - this is the order now
        assertEquals(8, lruCache.get(6).getValue());
        assertNull(lruCache.get(4));
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(null));
    }

    /**
     *
     */
    @Test
    void getPutInMruAlgorithm() {
         // 0..3,9 are the elements after init and size is 5
        assertEquals(9, mruCache.get(9).getKey());
        assertEquals(0, mruCache.get(0).getKey());
        assertEquals(5, mruCache.size());
        // {1,2,3,9,0}
        mruCache.cache(3,8);
        // {1,2,9,0,3->8} - so mru algorithm will delete 3
        assertEquals(8, mruCache.delete().getValue());
        assertNull(lruCache.get(4));
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(null));
    }

    /**
     *
     */
    @Test
    void deleteInLruAlgorithm() {
        // 5..9 are the elements after init
        // move 5 to the tail
        assertEquals(5, lruCache.get(5).getKey());
        // Now {6,7,8,9,5}
        // 6 is now a head (lru deletes it)
        assertEquals(6, lruCache.delete().getKey());
        assertEquals(8, lruCache.delete(8).getKey());
        assertEquals(3, lruCache.size());
        assertNull(lruCache.delete(8));
    }

    /**
     *
     */
    @Test
    void deleteInMruAlgorithm() {
        // 0..3,9 are the elements after init and size is 5
        // move 2 to the tail
        assertEquals(2, mruCache.get(2).getKey());
        // Now {0,1,3,9,2} 2 to be deleted by mru
        assertEquals(2, mruCache.delete().getKey());
        // Now {0,1,3,9}
        assertEquals(3, mruCache.delete(3).getKey());
        assertEquals(3, mruCache.size());
        assertNull(mruCache.delete(8));
    }

    /**
     *
     */
    @Test
    void size() {
        assertTrue(MemoryCache.DEFAULT_CACHE_SIZE >= lruCache.size());
        assertEquals(5, lruCache.size());
    }

    /**
     *
     */
    @Test
    void clearCache() {
        lruCache.clear();
        assertEquals(0, lruCache.size());
        assertNull(lruCache.delete(5));
        assertNull(lruCache.get(5));
    }
}