package com.lxgolovin.cache;

// TODO: To be documented
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    /**
     *
     */
    @BeforeEach
    void setUp() {
        int maxRange = 9;
        CacheAlgorithm<Integer> lru = new LruAlgorithm<>();
        lruCache = new MemoryCache<>(lru);

        CacheAlgorithm<Integer> mru = new MruAlgorithm<>();
        mruCache = new MemoryCache<>(mru, 0, 0);

        IntStream.rangeClosed(1,maxRange).forEach(x->lruCache.cache(x,x));
        IntStream.rangeClosed(1,maxRange).forEach(x->mruCache.cache(x,x));
    }

    /**
     *
     */
//    @Test
   /* void getInLruAlgorithm() {
        // 5..9 are the elements after init and size is 5
        assertEquals(5, lruCache.get(5));
        assertEquals(9, lruCache.get(9));
        assertEquals(5, lruCache.size());
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(4) );
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(null) );
    }*/

    /**
     *
     */
//    @Test
//    void getInMruAlgorithm() {
//        // 0..3,9 are the elements after init and size is 5
//        assertEquals(9, mruCache.get(9));
//        assertEquals(0, mruCache.get(0));
//        assertEquals(5, mruCache.size());
//        assertThrows(IllegalArgumentException.class,
//                () -> lruCache.get(4) );
//        assertThrows(IllegalArgumentException.class,
//                () -> lruCache.get(null) );
//    }

    /**
     *
     */
//    @Test
//    void deleteInLruAlgorithm() {
//        // 5..9 are the elements after init
//        // move 5 to the tail
//        assertEquals(5, lruCache.get(5));
//        // Now {6,7,8,9,5}
//        // 6 is now a head (lru deletes it)
//        assertEquals(6, lruCache.delete());
//        assertEquals(8, lruCache.delete(8));
//        assertEquals(3, lruCache.size());
//        assertThrows(IllegalArgumentException.class,
//                () -> lruCache.delete(8) );
//
//    }

    /**
     *
     */
//    @Test
//    void deleteInMruAlgorithm() {
//        // 0..3,9 are the elements after init and size is 5
//        // move 2 to the tail
//        assertEquals(2, mruCache.get(2));
//        // Now {0,1,3,9,2} 2 to be deleted by mru
//        assertEquals(2, mruCache.delete());
//        // Now {0,1,3,9}
//        assertEquals(3, mruCache.delete(3));
//        assertEquals(3, mruCache.size());
//        assertThrows(IllegalArgumentException.class,
//                () -> mruCache.delete(8) );
//
//    }

    /**
     *
     */
//    @Test
//    void size() {
//        assertTrue(MemoryCache.DEFAULT_CACHE_SIZE >= lruCache.size());
//        assertEquals(5, lruCache.size());
//    }
//
//    /**
//     *
//     */
//    @Test
//    void clearCache() {
//        lruCache.clear();
//        assertEquals(0, lruCache.size());
//        assertThrows(IllegalArgumentException.class,
//                () -> lruCache.delete(5) );
//        assertThrows(IllegalArgumentException.class,
//                () -> lruCache.get(5) );
//    }
}