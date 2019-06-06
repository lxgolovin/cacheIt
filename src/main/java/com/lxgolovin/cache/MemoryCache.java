package com.lxgolovin.cache;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of interface {@link Cache}. This class creates realization of memory cache.
 * The implementation is done using {@link HashMap}, where data is stored.
 * Value are stored in memory and removed or kept using different algorithms. As algorithms
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
public class MemoryCache<K, V> implements Cache<K, V>  {

    /**
     * Default cache size
     */
    static final int DEFAULT_CACHE_SIZE = 5;

    /**
     * Map to keep data
     */
    private final Map<K, V> cacheMap;

    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link MemoryCache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    private final int maxSize;

    /**
     * Defines cache algorithm
     */
    private final CacheAlgorithm<K> algo;

    /**
     * Creates memory cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    //! add one more constructor with Map as parameter.
    public MemoryCache(CacheAlgorithm<K> algorithm) {
        this(algorithm, DEFAULT_CACHE_SIZE);
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
     * Creates memory cache with default size by defined algorithm and by size.
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link MemoryCache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     */
    //! add one more constructor with Map as parameter.
    public MemoryCache(CacheAlgorithm<K> algorithm, int size) {
        cacheMap = new HashMap<>();
        algo = algorithm;
        maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
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

        Map.Entry<K, V> popped = null;
        Map.Entry<K, V> inputEntry = new AbstractMap.SimpleEntry<>(key, value);
        if (size() == maxSize) {
            // using deletion by algorithm
            popped = pop();
        }

        algo.shift(key);
        value = cacheMap.put(key, value); //! Do not mix up the logic in one line
        inputEntry.setValue(value);

        return (popped == null) ? inputEntry : popped;
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
        algo.shift(key);
        return cacheMap.get(key);
    }

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false
     */
    @Override
    public boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * To delete {@link Cache#delete(Object)} is used
     * @return popped out entry
     */
    @Override
    public Map.Entry<K, V> pop() {
        Map.Entry<K, V> entry;

        K key = algo.pop();
        V value = delete(key);
        entry = new AbstractMap.SimpleEntry<>(key, value);

        return entry;
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

        algo.delete(key);
        return cacheMap.remove(key);
    }

    /**
     * Clears all data from the queue
     * All elements are deleted. Elements in the algorithm queue are also deleted
     */
    @Override
    public void clear(){
        cacheMap.clear(); //! Why is it here clear() and in the next line flash()
        algo.clear();
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return cacheMap.size();
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return maxSize;
    }
}