package com.lxgolovin.cache.storage;

import java.util.HashMap;
import java.util.Map;

public class MemoryStorage<K, V> implements Storage<K, V>{

    /**
     * Map to keep data
     */
    private final Map<K, V> storageMap;

    public MemoryStorage() {
        storageMap = new HashMap<>();
    }

    public MemoryStorage(Map<K, V> map) {
        storageMap = (map == null) ? new HashMap<>() : map;
    }

    /**
     * IF the storage is not empty, one can get all the data from it
     * as a map
     * @return map of key-values, stored in storage
     */
    public Map<K, V> getAll() {
        return new HashMap<>(storageMap);
    }

    /**
     * Puts key-value mapping to the storage
     * @param key cannot be null
     * @param value cannot be null
     * @return if mapping key-value is present, return old value for the key.
     *          if there was no mapping, returns null
     * @throws IllegalArgumentException if any key or value is null
     */
    public V put(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        return storageMap.put(key, value);
    }

    /**
     * Gets value by key from the storage
     * @param key cannot be null
     * @return if mapping key-value is present, return value for the key.
     *          if there was no mapping, returns null
     * @throws IllegalArgumentException if key is null
     */
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return storageMap.get(key);
    }

    /**
     * @param key if null returns false
     * @return true if element found, else false
     */
    public boolean containsKey(K key) {
        return ((key != null) && storageMap.containsKey(key));
    }

    /**
     * @param key cannot be null
     * @return the previous value associated with key, or
     *         null if there was no mapping for key.
     */
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

    public boolean isEmpty() {
        return storageMap.isEmpty();
    }
}
