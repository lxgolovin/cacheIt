package com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class LRU<K, V> implements CacheAlgo<K,V> {

    private Map lruMap;

    public LRU() {
        lruMap = new HashMap<>();
    }

    public LRU(K key, V value) {
        lruMap = new HashMap<>();
    }

    @Override
    public K head() {
        return null;
    }

    @Override
    public K tail() {
        return null;
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link LRU#lruMap} does not contain the key
     */
    @Override
    public K add(K key, V value) {
        if ( key != null & value != null ) {
            lruMap.put(key, value);
            return key;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link LRU#lruMap} does not contain the key
     */
    @Override
    public K renew(K key, V value) {
        if ( ( key != null & value != null ) && lruMap.containsKey(key)  ) {
            lruMap.put(key, value);
            return key;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link LRU#lruMap} does not contain the key
     */
    @Override
    public K del(K key) {
        if ( key != null && lruMap.containsKey(key)) {
            lruMap.remove(key);
            return key;
        }
        throw new IllegalArgumentException();
    }
}
