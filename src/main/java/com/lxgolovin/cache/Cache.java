package com.lxgolovin.cache;

import java.util.AbstractMap;
import java.util.Map;

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
    K cache(K key, V value);

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
    V get(K key);

    /**
     *
     * @return
     */
    K delete();

    AbstractMap.SimpleEntry<K,V> deleteSE();

    /**
     *
     * @param key
     * @return
     */
    K delete(K key);

    /**
     *
     * @param entry
     * @return
     */
    AbstractMap.SimpleEntry<K,V> deleteSE(K key);

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
