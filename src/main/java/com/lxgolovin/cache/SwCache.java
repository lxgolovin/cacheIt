package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.MemoryStorage;
import com.lxgolovin.cache.storage.Storage;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of interface {@link Cache}. This class creates realization of cache.
 * The default implementation is done using {@link Storage}, where data is stored.
 * Values are stored in storage and removed or kept using different algorithms. As algorithms
 * interface {@link CacheAlgorithm} is used. Also default size is set for the cache, but the
 * size could be set by user. Note that size should by greater then 1, as cache with size 1 has no
 * sense.
 * By default the size of the cache is {@link Cache#DEFAULT_CACHE_SIZE} and the implementation is memory
 * cache {@link MemoryStorage}. But these parameters could be defined during initialization phase
 *
 * Here got methods to cache, delete, pop data by key. Has a possibility to clean data,
 * get maximum available size to current size.
 * @param <K>
 * @param <V>
 * @see Cache
 * @see CacheAlgorithm
 * @see Storage
 * @see MemoryStorage
 * @see FileSystemStorage
 */
public class SwCache<K, V> implements Cache<K, V> {
    
    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link Cache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    private final int maxSize;

    /**
     * Defines cache algorithm
     */
    private final CacheAlgorithm<K> algorithm;

    /**
     * Storage to keep key-values
     */
    private final Storage<K, V> storage;

    /**
     * Creates memory cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public SwCache(CacheAlgorithm<K> algorithm) {
        this(algorithm, new MemoryStorage<>(), new HashMap<>(), Cache.DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * If the map is empty, empty cache is created with maxSize {@link Cache#DEFAULT_CACHE_SIZE}.
     * If the map is not empty, cache is created with maxSize equal to the size of the incoming map.
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     */
    public SwCache(CacheAlgorithm<K> algorithm, Map<K, V> map) {
        this(algorithm, null, map, Cache.DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with default size by defined algorithm.
     * Here key-value for the first element are defined
     * @param algorithm specifies algorithm type that is used by the cache
     * @param key specifies key for the entry
     * @param value defined value inside entry
     */
    public SwCache(CacheAlgorithm<K> algorithm, K key, V value) {
        this(algorithm, null, new HashMap<>(), Cache.DEFAULT_CACHE_SIZE);
        cache(key, value);
    }

    /**
     * Creates memory cache with defined algorithm and size.
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public SwCache(CacheAlgorithm<K> algorithm, int size) {
        this(algorithm, null, new HashMap<>(), size);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     * @param size defining the size for the mapping
     */
    private SwCache(CacheAlgorithm<K> algorithm, Storage<K, V> storage, Map<K, V> map, int size) {
        if ((algorithm == null) | (map == null)) {
            throw new IllegalArgumentException();
        }

        this.algorithm = algorithm;
        this.storage = (storage == null) ? new MemoryStorage<>() : storage;

        if (!map.isEmpty()) {
            maxSize = map.size();
        } else {
            maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
        }
        
        map.forEach(this.storage::put);
        putAll(map);
    }

    /**
     * Put all values of map into cache
     * @param map with key-values
     */
    private void putAll(Map<K, V> map) {
        map.keySet().forEach(algorithm::shift);
    }

    /**
     * Caches data into cache by key value. If cache is full up, data is removed from
     * cache using some algorithm
     * @param key to define data to be loaded to cache
     * @param value to be loaded to cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         If any key-value mapping was popped during this task, because of size limit,
     *         the deleted key-value mapping will be returned.
     * @throws IllegalArgumentException if any of incoming parameters are null
     */
    @Override
    public Map.Entry<K, V> cache(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        Map.Entry<K, V> poppedEntry = null;
        if ((size() == maxSize) && (!contains(key))) {
            // using deletion by algorithm
            poppedEntry = pop();
        }

        algorithm.shift(key);
        value = storage.put(key, value);
        Map.Entry<K, V> replacedEntry = null;
        if (value != null) {
            replacedEntry = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }

        return (poppedEntry == null) ? replacedEntry : poppedEntry;
    }

    /**
     * Gets value by the key
     * @param key - may not be null
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws IllegalArgumentException if key is null
     */
    @Override
    public V get(K key){
        if (key == null) {
            throw new IllegalArgumentException();
        }
        // Need to move key as it was accessed
        algorithm.shift(key);
        return storage.get(key);
    }

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false. Returns false if key is null
     */
    @Override
    public boolean contains(K key) {
        return ((key != null) && storage.containsKey(key));
    }

    /**
     * Removes the mapping for a key from this cache. Does not depend on algorithm type
     *
     * <p>Returns the value for the associated key,
     * or <tt>null</tt> if the cache contained no mapping for the key.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public V delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        algorithm.delete(key);
        return storage.remove(key);
    }

    /**
     * Clears all data from the queue
     * All elements are deleted. Elements in the algorithm queue are also deleted
     */
    @Override
    public void clear(){
        storage.clear();
        algorithm.clear();
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * To delete {@link Cache#delete(Object)} is used
     * @return popped out entry, returns null entry if the element was not
     *          found in algorithm queue (empty)
     */
    @Override
    public Map.Entry<K, V> pop() {
        K key = algorithm.pop();
        if (key == null) {
            return null;
        }

        Map.Entry<K, V> entry;
        V value = delete(key);
        entry = new AbstractMap.SimpleImmutableEntry<>(key, value);

        return entry;
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return maxSize;
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return storage.size();
    }
}