package com.lxgolovin.cache.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link Storage} to keep data in memory
 * @see Storage
 * @see FileSystemStorage
 */
public class MemoryStorage<K, V> implements Storage<K, V>{

    private final Map<K, V> storageMap;

    public MemoryStorage() {
        storageMap = new HashMap<>();
    }

    public MemoryStorage(Map<K, V> map) {
        storageMap = (map == null) ? new HashMap<>() : map;
    }

    /**
     * If the storage is not empty, one can get all the data from it
     * as a map
     * @return map of key-values, stored in storage
     */
    public Map<K, V> getAll() {
        return new HashMap<>(storageMap);
    }

    /**
     * @param key cannot be null
     * @param value cannot be null
     * @throws IllegalArgumentException if any key or value is null
     */
    public Optional<V> put(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        return Optional.ofNullable(storageMap.put(key, value));
    }

    /**
     * @param key cannot be null
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return Optional.ofNullable(storageMap.get(key));
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
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return Optional.ofNullable(storageMap.remove(key));
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
