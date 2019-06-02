package com.lxgolovin.cache;

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
     * @param key
     * @return
     */
    V get(K key);

    /**
     *
     * @return
     */
    K delete();

    /**
     *
     * @param key
     * @return
     */
    K delete(K key);

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
