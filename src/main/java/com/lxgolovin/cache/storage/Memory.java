package com.lxgolovin.cache.storage;

import java.util.HashMap;
import java.util.Map;

public class Memory<K, V> implements Storage<K, V>{

    /**
     * Map to keep data
     */
    private final Map<K, V> cacheMap;

    public Memory() {
        cacheMap = new HashMap<>();
    }

    public Memory(Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }

        cacheMap = map;
    }

    public V put(K key, V value) {
        return cacheMap.put(key, value);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }

    public boolean containsKey(K key) {
        return ((key != null) && cacheMap.containsKey(key));
    }

    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return cacheMap.remove(key);
    }

    public void clear(){
        cacheMap.clear();
    }

    public int size() {
        return cacheMap.size();
    }
}
