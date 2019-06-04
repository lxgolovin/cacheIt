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
     * @param key
     * @param value
     * @return
     */
    AbstractMap.SimpleEntry<K, V> cache(K key, V value);

    /**
     *
     * @param entry
     * @return
     */
    AbstractMap.SimpleEntry<K, V> cache(AbstractMap.SimpleEntry<K,V> entry);

    /**
     *
     * @param key
     * @return
     */
    AbstractMap.SimpleEntry<K, V> get(K key);

    /**
     *
     * @return
     */
    AbstractMap.SimpleEntry<K,V> delete();

    /**
     *
     * @param key
     * @return
     */
    AbstractMap.SimpleEntry<K,V> delete(K key);

    /**
     *
     * @param entry
     * @return
     */
    AbstractMap.SimpleEntry<K,V> delete(AbstractMap.SimpleEntry<K,V> entry);

    /**
     *
     */
    void clear();

    /**
     *
     * @return
     */
    int size();
}
