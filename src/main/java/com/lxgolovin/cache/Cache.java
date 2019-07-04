package com.lxgolovin.cache;

import com.lxgolovin.cache.type.AbstractCache;

import java.util.Map;

/**
 * Interface to describe cache. Creates methods to cache, delete, pop data
 * by key. Has a possibility to clean data, get maximum available size to current size.
 * @param <K>
 * @param <V>
 * @see AbstractCache
 */
public interface Cache<K, V> {

    /**
     * Default cache size
     */
    int DEFAULT_CACHE_SIZE = 5;

    /**
     * Caches data into cache by key value. If cache is full up, data is removed (popped out) from
     * cache using some algorithm
     * @param key to define data to be loaded to cache
     * @param value to be loaded to cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         If any key-value mapping was popped during this task, because of size limit,
     *         the deleted key-value mapping will be returned.
     */
    Map.Entry<K, V> cache(K key, V value);

    /**
     * Gets value by the key
     * @param key with mapping in cache to value
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    V get(K key);

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * @return popped out entry, returns null entry if the element was not
     *          found in algorithm queue (empty)
     */
    Map.Entry<K, V> pop();

    /**
     * Removes the mapping for a key from this cache. Does not depend on algorithm type
     *
     * <p>Returns the value for the associated key,
     * or <tt>null</tt> if the cache contained no mapping for the key.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    V delete(K key);

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false
     */
    boolean contains(K key);

    /**
     * Clears all data from the queue
     * All elements are deleted.
     */
    void clear();

    /**
     * @return current size of the cache
     */
    int size();

    /**
     * @return maximum possible size of the cache
     */
    int sizeMax();
}
