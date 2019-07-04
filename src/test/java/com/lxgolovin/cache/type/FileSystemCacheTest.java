package com.lxgolovin.cache.type;

import com.lxgolovin.cache.Cache;
import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.algorithm.Mru;
import com.lxgolovin.cache.type.FileStorage;
import com.lxgolovin.cache.type.FileSystemCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create checks-tests for the implementation of file system cache {@link FileSystemCache}
 * based on interface {@link Cache}. Two algorithms are used for testing: LRU and MRU.
 * Algorithms are defined be interface {@link CacheAlgorithm} with implementations
 * {@link Lru} and {@link Mru}
 * @see Cache
 * @see FileSystemCache
 * @see CacheAlgorithm
 * @see Lru
 * @see Mru
 */
class FileSystemCacheTest {

    /**
     * Algorihtm types use in testing
     */
    private final CacheAlgorithm<Integer> lru = new Lru<>();
    private final CacheAlgorithm<Integer> mru = new Mru<>();

    /**
     * LRU cache will be initialised with size 6
     */
    private final int maxSize = Cache.DEFAULT_CACHE_SIZE + 1;

    /**
     * caches
     */
    private final Cache<Integer, Integer> lruCache = new FileSystemCache<>(lru, maxSize);
    private final Cache<Integer, Integer> mruCache = new FileSystemCache<>(mru, 0, 0);

    /**
     * Fill in caches.
     * LRU cache is initialized
     * {4->4, 5->5, 6->6, 7->7, 8->8, 9->9}
     * MRU cache is initialized
     * {0->0, 1->1, 2->2, 3->3, 9->9}
     */
    @BeforeEach
    void setUp() {
        int maxRange = 9;
        IntStream.rangeClosed(1, maxRange).forEach(x->lruCache.cache(x,x));
        IntStream.rangeClosed(1, maxRange).forEach(x->mruCache.cache(x,x));
    }

    /**
     * Checks work of setting cache size manually
     */
    @Test
    void constructorWithDefaultSize() {
        Cache<Integer, String> cache = new FileSystemCache<>(lru);
        assertEquals(0, cache.size());
        assertEquals(5, cache.sizeMax());
    }

    /**
     * Checks work of setting cache size manually
     */
    @Test
    void constructorWithMaps() {
        CacheAlgorithm<Integer> algorithm = new Lru<>();
        Map<Integer, String> map = new TreeMap<>();
        IntStream.rangeClosed(1, 10).forEach(x -> map.put(x,String.valueOf(x*x)));

        Cache<Integer, String> cache = new FileSystemCache<>(algorithm,map);
        assertEquals(1,cache.cache(36, "36").getKey());
        assertEquals(10, cache.size());
        assertEquals(10, cache.sizeMax());
    }

    /**
     * Put values in cache and delete them to check if algorithm in
     * cache works fine
     */
    @Test
   void getPutInLruAlgorithm() {
        // 4..9 are the elements after init and size is 6
        assertEquals(5, lruCache.get(5));
        // Now {4,6,7,8,9,5}
        assertEquals(9, lruCache.get(9));
        // Now {4,6,7,8,5,9}
        assertEquals(6, lruCache.size());
        // Now {4,6,7,8,5,9} and value for 6 is 6. Change value to 36
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleImmutableEntry<>(6, 6);
        assertEquals(testEntry, lruCache.cache(6, 36));
        // Now {4,7,8,5,9,6} and value for 6 was 6, but it was replaced. Inside cache value now is 36
        assertEquals(36, lruCache.get(6) );
        // Now {4,7,8,5,9,6} LRU will delete 4. Check this out
        assertEquals(4, lruCache.pop().getKey());
        // Now {7,8,5,9,6}; 4 was deleted, let's call for it again. Null should be
        assertNull(lruCache.get(4));
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(null));
    }

    /**
     * Put values in cache and delete them to check if algorithm in
     * cache works fine
     */
    @Test
    void getPutInMruAlgorithm() {
        // 0..3, 9 are the elements after init and size is 5
        assertNull(mruCache.get(5));
        // Now {0,1,2,3,9} check 9 is present
        assertEquals(9, mruCache.get(9));
        // Now {0,1,2,3,9}
        assertEquals(5, mruCache.size());
        // Now {0,1,2,3,9} and value for 2 is 2. Change value to 4
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleImmutableEntry<>(2, 2);
        assertEquals(testEntry, mruCache.cache(2, 4));
        // Now {0,1,3,9,2} and value for 2 was 2, but it was replaced. Inside cache value now is 4
        assertEquals(4, mruCache.get(2) );
        // Now {0,1,3,9,2} MRU will delete 2. Check this out
        assertEquals(2, mruCache.pop().getKey());
        // Now {0,1,3,9}; 2 was deleted, let's call for it again. Null should be
        assertNull(lruCache.get(2));
        assertThrows(IllegalArgumentException.class,
                () -> lruCache.get(null));
    }

    /**
     * Check deleting from cache. Also deleting till empty
     */
    @Test
    void deleteInLruAlgorithm() {
        // 4..9 are the elements after init and size is 6
        // move 5 to the tail
        assertEquals(5, lruCache.get(5));
        // Now {4,6,7,8,9,5}
        // 4 is now a head (lru deletes it)
        assertEquals(4, lruCache.pop().getKey());
        // Now {6,7,8,9,5}
        assertEquals(8, lruCache.delete(8));
        // Now {6,7,9,5}
        assertEquals(4, lruCache.size());
        // delete 8 one more time
        assertNull(lruCache.delete(8));
        // delete all one by one
        assertEquals(6, lruCache.delete(6));
        assertEquals(7, lruCache.delete(7));
        assertEquals(9, lruCache.delete(9));
        // Now {5->5}
        lruCache.cache(5,25);
        // Now {5->25} now delete and return value
        assertEquals(25, lruCache.delete(5));
        assertNull(lruCache.pop());
    }

    /**
     * Check deleting from cache. Also deleting till empty
     */
    @Test
    void deleteInMruAlgorithm() {
        // 0..3, 9 are the elements after init and size is 5
        // move 3 to the tail
        assertEquals(3, mruCache.get(3));
        // Now {0,1,2,9,3}
        // 3 is now a head (mru deletes it)
        assertEquals(3, mruCache.pop().getKey());
        // Now {0,1,2,9,}
        assertEquals(9, mruCache.delete(9));
        // Now {0,1,2}
        assertEquals(3, mruCache.size());
        // delete 9 one more time
        assertNull(mruCache.delete(9));
        // delete all one by one
        assertEquals(1, mruCache.delete(1));
        assertEquals(0, mruCache.delete(0));
        // Now {2->2}
        mruCache.cache(2,4);
        // Now {2->4} now delete and return value
        assertEquals(4, mruCache.delete(2));
        assertNull(mruCache.pop());
    }

    /**
     * Checks maximum size and the current size of the cache
     */
    @Test
    void size() {
        assertTrue(maxSize >= lruCache.size());
        assertEquals(maxSize, lruCache.size());
    }

    /**
     * Testing clearing cache and then manual deleting of the elements
     */
    @Test
    void clearCache() {
        lruCache.clear();
        assertEquals(0, lruCache.size());
        assertNull(lruCache.delete(5));
        assertNull(lruCache.get(5));
    }

    /**
     * Testing constructor with defining path at initialization phase
     */
    @Test
    void constructorWithPathDefined() {
        String directory = "./TEMP/";
        Path directoryPath = Paths.get(directory);

        FileStorage<Integer, String> fileStorage = new FileStorage<>(directoryPath);
        // create 4 files with some data
        for (int i=0; i<5; i++) {
            Path path = fileStorage.createFile();
            assertTrue(fileStorage.writeToFile(i, String.valueOf(i), path));
        }

        CacheAlgorithm<Integer> fsLru = new Lru<>();
        Cache<Integer, String> fsCache = new FileSystemCache<>(fsLru, directoryPath);
        assertEquals("1", fsCache.get(1));
    }
}