package com.lxgolovin.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MemoryCacheTest {
    private final static int RANGE_MAX = 10;
    private final static int CACHE_CAPACITY = 5;
    private Cache<Integer, Integer> lruCache;

    @BeforeEach
    void setUp() {
        lruCache = new MemoryCache<>();
        IntStream.rangeClosed(1,RANGE_MAX).forEach(x->lruCache.cache(x,x));
    }

    @Test
    void get() {
        assertEquals(5, lruCache.get(5));
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.get(1000); }  );
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.get(null); }  );
    }

    @Test
    void delete() {
        assertEquals(5, lruCache.get(5));
        assertEquals(5, lruCache.delete(5));
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.delete(5); }  );
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.delete(1000); }  );

    }

    @Test
    void size() {
        assertEquals(CACHE_CAPACITY,lruCache.size());
    }

    @Test
    void clearCache() {
        lruCache.clear();
        assertEquals(0, lruCache.size());
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.delete(5); }  );
        assertThrows(IllegalArgumentException.class,
                () -> { lruCache.get(5); }  );

    }
}