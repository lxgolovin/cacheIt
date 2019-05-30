package com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache<K, V> implements Cache<K, V>  {

    private Map<K, V> cacheMap;


    public MemoryCache() {
        cacheMap = new HashMap<>();
    }

    public MemoryCache(K key, V value) {
        cacheMap = new HashMap<>();
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
            cacheMap.put(key, value);
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
            return cacheMap.get(key);
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
            cacheMap.remove(key);
            return key;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void clear(){
        cacheMap.clear();
    }

    @Override
    public int size() {
        return cacheMap.size();
    }
}
