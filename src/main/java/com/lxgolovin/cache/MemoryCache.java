package com.lxgolovin.cache;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of interface {@link Cache}. This class creates realization of memory cache.
 * The implementation is done using {@link HashMap}, where data is stored.
 * Values are stored in memory and removed or kept using different algorithms. As algorithms
 * interface {@link CacheAlgorithm} is used. Also default size is set for the cache, but the
 * size could be set by user. Note that size should by greater then 1, as cache with size 1 has no
 * sense.
 *
 * Here got methods to cache, delete, pop data by key. Has a possibility to clean data,
 * get maximum available size to current size.
 * @param <K>
 * @param <V>
 * @see Cache
 * @see CacheAlgorithm
 */
<<<<<<< HEAD
public class MemoryCache<K, V> implements Cache<K, V>  {


    /**
     * Map to keep data
     */
    protected final Map<K, V> cacheMap;

    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link Cache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    protected final int maxSize;

    /**
     * Defines cache algorithm
     */
    protected final CacheAlgorithm<K> algo;

    /**
     * Creates memory cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm) {
        this(algorithm, new HashMap<>(), DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * If the map is empty, empty cache is created with maxSize {@link Cache#DEFAULT_CACHE_SIZE}.
     * If the map is not empty, cache is created with maxSize equal to the size of the incoming map.
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, Map<K, V> map) {
        this(algorithm, map, map.size());
    }

    /**
     * Creates memory cache with default size and using defined algorithm.
     * Here key-value for the first element are defined
     * @param algorithm specifies algorithm type that is used by the cache
     * @param key specifies key for the entry
     * @param value defined value inside entry
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, K key, V value) {
        this(algorithm, DEFAULT_CACHE_SIZE);
        cache(key, value);
    }

    /**
     * Creates memory cache with defined algorithm and size.
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, int size) {
        this(algorithm, new HashMap<>(), size);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     * @param size defining the size for the mapping
     */
    MemoryCache(CacheAlgorithm<K> algorithm, Map<K, V> map, int size) {
        maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
        algo = algorithm;
        cacheMap = map;
        if (!map.isEmpty()) {
            map.keySet().forEach(algo::shift);
        }
    }
}