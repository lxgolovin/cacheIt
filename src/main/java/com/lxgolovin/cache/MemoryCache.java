package com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache<K, V> implements Cache<K, V>  {

    /**
     * Default cache size
     */
    private static final int DEFAULT_CACHE_SIZE = 10;

    /**
     * LRU algorithm
     */
    private static final String LRU_ALGORITHM = "LRU";

    /**
     * MRU algorithm
     */
    private static final String MRU_ALGORITHM = "MRU";

    /**
     * Default algorithm type if none is defined in constructor
     */
    private static final String DEFAULT_ALGORITHM_TYPE = LRU_ALGORITHM;


    /**
     * Map to keep data
     */
    private Map<K, V> cacheMap;

    /**
     *
     */
    CacheAlgorithm<K> algo;


    public MemoryCache() {
        cacheMap = new HashMap<>();
        algo = new LruMru<>();
    }

    public MemoryCache(K key, V value) {
        cacheMap = new HashMap<>();
        algo = new LruMru<>();
        cache(key, value);
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public K cache(K key, V value) {
        if ( key != null & value != null ) {
            cacheMap.put(algo.shift(key), value);
            return key;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public V get(K key){
        if ( key != null && cacheMap.containsKey(key) ) {
            return cacheMap.get(algo.shift(key));
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public K delete(K key) {
        if ( key != null && cacheMap.containsKey(key)) {
            cacheMap.remove(algo.unshift(key));
            return key;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void clear(){
        cacheMap.clear();
        algo.flash();
    }

    @Override
    public int size() {
        return cacheMap.size();
    }
}
