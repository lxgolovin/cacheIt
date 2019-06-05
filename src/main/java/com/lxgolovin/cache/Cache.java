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
    //! Better Map.Entry. Why do you need to return the same as you inserted?
    AbstractMap.SimpleEntry<K, V> cache(K key, V value);

    /**
     *
     * @param entry with data to be loaded to cache
     * @return deleted entry or the inserted one
     */
    //! Remove it
    AbstractMap.SimpleEntry<K, V> cache(AbstractMap.SimpleEntry<K,V> entry);

    /**
     * @param key to define the entry
     * @return the entry by the key
     */
    //! Do you need a key in return?
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
    //! Do you need a key in return?
    AbstractMap.SimpleEntry<K,V> delete(K key);

    /**
     *
     * @param entry with data to be deleted from cache
     * @return entry deleted by the key
     */
    //! The return and the parameter are the same thing
    AbstractMap.SimpleEntry<K,V> delete(AbstractMap.SimpleEntry<K,V> entry);

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false
     */
    boolean contains(K key);

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
