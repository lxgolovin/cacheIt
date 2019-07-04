package com.lxgolovin.cache.type;

import com.lxgolovin.cache.AbstractCache;
import com.lxgolovin.cache.Cache;
import com.lxgolovin.cache.algorithm.CacheAlgorithm;

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
public class MemoryCache<K, V> extends AbstractCache<K, V> implements Cache<K, V> {

    /**
     * Map to keep data
     */
    private final Map<K, V> cacheMap;

    /**
     * Creates memory cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm) {
        this(algorithm, DEFAULT_CACHE_SIZE);
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
        this(algorithm, map, DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with default size by defined algorithm.
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
    private MemoryCache(CacheAlgorithm<K> algorithm, Map<K, V> map, int size) {
        if ((algorithm == null) | (map == null)) {
            throw new IllegalArgumentException();
        }

        if (!map.isEmpty()) {
            maxSize = map.size();
        } else {
            maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
        }
        
        this.algorithm = algorithm;
        cacheMap = map;
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
        value = cacheMap.put(key, value);
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
        return cacheMap.get(key);
    }

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false. Returns false if key is null
     */
    @Override
    public boolean contains(K key) {
        return ((key != null) && cacheMap.containsKey(key));
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
        return cacheMap.remove(key);
    }

    /**
     * Clears all data from the queue
     * All elements are deleted. Elements in the algorithm queue are also deleted
     */
    @Override
    public void clear(){
        cacheMap.clear();
        algorithm.clear();
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return cacheMap.size();
    }
}