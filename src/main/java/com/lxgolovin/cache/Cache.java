package com.lxgolovin.cache;

import java.util.AbstractMap;

/**
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> {

    /**
     *
     * @param key to define data to be loaded to cache
     * @param value to be loaded to cache
     * @return deleted entry or the inserted one
     */
    AbstractMap.SimpleEntry<K, V> cache(K key, V value);

    /**
     *
     * @param entry with data to be loaded to cache
     * @return deleted entry or the inserted one
     */
    AbstractMap.SimpleEntry<K, V> cache(AbstractMap.SimpleEntry<K,V> entry);

    /**
     * @param key to define the entry
     * @return the entry by the key
     */
    AbstractMap.SimpleEntry<K, V> get(K key);

    /**
     *
     * @return entry deleted by the defined algorithm
     */
    AbstractMap.SimpleEntry<K,V> delete();

    /**
     * @param key - for the entry to be deleted from cache
     * @return entry deleted by the key
     */
    AbstractMap.SimpleEntry<K,V> delete(K key);

    /**
     *
     * @param entry with data to be deleted from cache
     * @return entry deleted by the key
     */
    AbstractMap.SimpleEntry<K,V> delete(AbstractMap.SimpleEntry<K,V> entry);

    /**
     *
     */
    void clear();

    /**
     *
     * @return current size of the cache
     */
    int size();

    /**
     * @return cache max size
     */
    int sizeMax();
}
