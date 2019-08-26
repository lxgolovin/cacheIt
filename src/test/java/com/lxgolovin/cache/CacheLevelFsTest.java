package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.algorithm.Mru;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.Storage;
import com.lxgolovin.cache.tools.ListGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create checks-tests for the implementation of memory cache {@link CacheLevel}
 * based on interface {@link Cache}. Two algorithms are used for testing: LRU and MRU.
 * Algorithms are defined be interface {@link CacheAlgorithm} with implementations
 * {@link Lru} and {@link Mru}
 * @see Cache
 * @see CacheLevel
 * @see CacheAlgorithm
 * @see Lru
 * @see Mru
 */
class CacheLevelFsTest {

    /**
     * Algorithm types use in testing
     */
    private final CacheAlgorithm<Integer> lru = new Lru<>();
    private final CacheAlgorithm<Integer> mru = new Mru<>();

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * LRU cache will be initialised with size 6
     */
    private final int maxSize = Cache.DEFAULT_CACHE_SIZE + 1;

    /**
     * caches with memory cache
     */
    private final Storage<Integer, Integer> lruStorage = new FileSystemStorage<>();
    private final Storage<Integer, Integer> mruStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);

    private final Cache<Integer, Integer> lruCache = new CacheLevel<>(lru, lruStorage, maxSize);
    private final Cache<Integer, Integer> mruCache = new CacheLevel<>(mru, mruStorage);

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
        for (int i = 1; i <= maxRange; i++) {
            lruCache.cache(i,i);
        }
//        IntStream.rangeClosed(1, maxRange).forEach(x->lruCache.cache(x,x));
        IntStream.rangeClosed(0, maxRange).forEach(x->mruCache.cache(x,x));
    }

    /**
     * Checks work of setting cache size manually
     */
    @Test
    void constructorWithDefaultSize() {
        Storage<Integer, String> storage = new FileSystemStorage<>();
        Cache<Integer, String> cache = new CacheLevel<>(lru, storage);
        assertEquals(0, cache.size());
        assertEquals(5, cache.sizeMax());
    }

    /**
     * Checks work with not empty map
     */
    @Test
    void constructorWithNotEmptyMap() {
        Map<Integer, Integer> map = new TreeMap<>();
        IntStream.rangeClosed(1, 10).forEach(x -> map.put(x, (x*x)));

        Storage<Integer, Integer> notEmptyStorage = new FileSystemStorage<>(Paths.get(directoryPath), map);
        assertEquals(10, notEmptyStorage.size());

        CacheAlgorithm<Integer> lru = new Lru<>();
        Cache<Integer, Integer> cache = new CacheLevel<>(lru, notEmptyStorage);
        assertEquals(Optional.of(1),cache.cache(36, 36).map(Map.Entry::getValue));
        assertEquals(10, cache.size());
        assertEquals(10, cache.sizeMax());
    }

    @Test
    void putRandomDataIntoCache() {
        final int dataListSize = 1000;
        List<Integer> data = ListGenerator.generateInt(dataListSize);

        data.forEach(k -> {
            int v = (int) (Math.random() * dataListSize);
            lruCache.cache(k, v);
            mruCache.cache(k, v);
        });

        assertEquals(lruCache.size(), lruCache.sizeMax());
        assertEquals(mruCache.size(), mruCache.sizeMax());
    }

    /**
     * Reads data from existing directory. In this case storage is not empty
     * The size is set to the number of files in the storage
     */
    @Test
    void constructorWithNonEmptyStorage() {
        Storage<Integer, Integer> notEmptyStorage = new FileSystemStorage<>(Paths.get(directoryPath));
        assertEquals(0, notEmptyStorage.size());

        CacheAlgorithm<Integer> lru = new Lru<>();
        Cache<Integer, Integer> cache = new CacheLevel<>(lru, notEmptyStorage);

        assertTrue(!notEmptyStorage.isEmpty());
        assertEquals(notEmptyStorage.size(), cache.size());
        assertEquals(notEmptyStorage.size(), cache.sizeMax());
    }

    /**
     * Put values in cache and delete them to check if algorithm in
     * cache works fine
     */
    @Test
    void getPutInLruAlgorithm() {
        // 4..9 are the elements after init and size is 6
        assertEquals(Optional.of(5), lruCache.get(5));
        // Now {4,6,7,8,9,5}
        assertEquals(Optional.of(9), lruCache.get(9));
        // Now {4,6,7,8,5,9}
        assertEquals(6, lruCache.size());
        // Now {4,6,7,8,5,9} and value for 6 is 6. Change value to 36
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleImmutableEntry<>(6, 6);
        assertEquals(Optional.of(testEntry), lruCache.cache(6, 36));
        // Now {4,7,8,5,9,6} and value for 6 was 6, but it was replaced. Inside cache value now is 36
        assertEquals(Optional.of(36), lruCache.get(6) );
        // Now {4,7,8,5,9,6} LRU will delete 4. Check this out
        assertEquals(Optional.of(4), lruCache.pop().map(Map.Entry::getKey));
        // Now {7,8,5,9,6}; 4 was deleted, let's call for it again. Null should be
        assertFalse(lruCache.get(4).isPresent());
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
        assertFalse(mruCache.get(5).isPresent());
        // Now {0,1,2,3,9} check 9 is present
        assertEquals(Optional.of(9), mruCache.get(9));
        // Now {0,1,2,3,9}
        assertEquals(5, mruCache.size());
        // Now {0,1,2,3,9} and value for 2 is 2. Change value to 4
        Map.Entry<Integer, Integer> testEntry = new AbstractMap.SimpleImmutableEntry<>(2, 2);
        assertEquals(Optional.of(testEntry), mruCache.cache(2, 4));
        // Now {0,1,3,9,2} and value for 2 was 2, but it was replaced. Inside cache value now is 4
        assertEquals(Optional.of(4), mruCache.get(2) );
        // Now {0,1,3,9,2} MRU will delete 2. Check this out
        assertEquals(Optional.of(2), mruCache.pop().map(Map.Entry::getKey));
        // Now {0,1,3,9}; 2 was deleted, let's call for it again. Null should be
        assertFalse(lruCache.get(2).isPresent());
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
        assertEquals(Optional.of(5), lruCache.get(5));
        // Now {4,6,7,8,9,5}
        // 4 is now a head (lru deletes it)
        assertEquals(4, lruCache.pop().get().getKey());
        // Now {6,7,8,9,5}
        assertEquals(8, lruCache.delete(8).get());
        // Now {6,7,9,5}
        assertEquals(4, lruCache.size());
        // delete 8 one more time
        assertFalse(lruCache.delete(8).isPresent());
        // delete all one by one
        assertEquals(Optional.of(6), lruCache.delete(6));
        assertEquals(Optional.of(7), lruCache.delete(7));
        assertEquals(Optional.of(9), lruCache.delete(9));
        // Now {5->5}
        lruCache.cache(5,25);
        // Now {5->25} now delete and return value
        assertEquals(Optional.of(25), lruCache.delete(5));
        assertFalse(lruCache.pop().isPresent());
    }

    /**
     * Check deleting from cache. Also deleting till empty
     */
    @Test
    void deleteInMruAlgorithm() {
        // 0..3, 9 are the elements after init and size is 5
        // move 3 to the tail
        assertEquals(Optional.of(3), mruCache.get(3));
        // Now {0,1,2,9,3}
        // 3 is now a head (mru deletes it)
        assertEquals(3, mruCache.pop().get().getKey());
        // Now {0,1,2,9,}
        assertEquals(9, mruCache.delete(9).get());
        // Now {0,1,2}
        assertEquals(3, mruCache.size());
        // delete 9 one more time
        assertFalse(mruCache.delete(9).isPresent());
        // delete all one by one
        assertEquals(Optional.of(1), mruCache.delete(1));
        assertEquals(Optional.of(0), mruCache.delete(0));
        // Now {2->2}
        mruCache.cache(2,4);
        // Now {2->4} now delete and return value
        assertEquals(Optional.of(4), mruCache.delete(2));
        assertFalse(mruCache.pop().isPresent());
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
        assertFalse(lruCache.delete(5).isPresent());
        assertFalse(lruCache.get(5).isPresent());
    }

    /**
     * Testing behaviour is null is on input
     */
    @Test
    void nullInputs() {
        CacheAlgorithm<Integer> algorithm = new Lru<>();
        assertThrows(IllegalArgumentException.class, () -> new CacheLevel<>(null));
        assertThrows(IllegalArgumentException.class, () -> new CacheLevel<>(algorithm,null, null));
        assertThrows(IllegalArgumentException.class, () -> new CacheLevel<>(null, new FileSystemStorage<>()));

        Cache<Integer, Integer> memoryCache =  new CacheLevel<>(algorithm, new FileSystemStorage<>());
        assertThrows(IllegalArgumentException.class, () -> memoryCache.delete(null));
        assertThrows(IllegalArgumentException.class, () -> memoryCache.get(null));
        assertFalse(memoryCache.contains(null));
    }
}