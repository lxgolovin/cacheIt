package com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache<K, V> implements Cache<K, V>  {

    /**
     * Default cache size
     */
    private static final int DEFAULT_CACHE_SIZE = 5;

    /**
     * Map to keep data
     */
    private Map<K, V> cacheMap;

    /**
     *
     */
    private CacheAlgorithm<K> algo;

    /**
     *
     * @param algorithm
     */
    public MemoryCache(CacheAlgorithm<K> algorithm) {
        cacheMap = new HashMap<>();
        algo = algorithm;
    }

    /**
     *
     * @param algorithm
     * @param key
     * @param value
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, K key, V value) {
        cacheMap = new HashMap<>();
        algo = algorithm;
        cache(key, value);
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public K cache(K key, V value) {
        if ( key == null | value == null ) { throw new IllegalArgumentException(); }
        if ( size() == DEFAULT_CACHE_SIZE ) {
            // TODO: check if the key is already in cacheMap!
//            delete(key);
        }
        cacheMap.put(algo.shift(key), value);
        return key;
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public V get(K key){
        if ( key == null || !cacheMap.containsKey(key) ) { throw new IllegalArgumentException(); }
        return cacheMap.get(algo.shift(key));
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public K delete(K key) {
        if ( key == null || !cacheMap.containsKey(key) ) { throw new IllegalArgumentException(); }
        cacheMap.remove(algo.delete(key));
        return key;
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
