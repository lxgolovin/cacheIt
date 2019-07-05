package com.lxgolovin.cache.storage;

import java.util.HashMap;
import java.util.Map;

public class Memory<K, V> implements Storage<K, V>{

    /**
     * Map to keep data
     */
    private final Map<K, V> storageMap;

    public Memory() {
        storageMap = new HashMap<>();
    }

    public Memory(Map<K, V> map) {
        storageMap = (map == null) ? new HashMap<>() : map;
    }

    public V put(K key, V value) {
        return storageMap.put(key, value);
    }

    public V get(K key) {
        return storageMap.get(key);
    }

    public boolean containsKey(K key) {
        return ((key != null) && storageMap.containsKey(key));
    }

    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return storageMap.remove(key);
    }

    public void clear(){
        storageMap.clear();
    }

    public int size() {
        return storageMap.size();
    }
    
    public boolean isEmpty() { return storageMap.isEmpty(); }
}
