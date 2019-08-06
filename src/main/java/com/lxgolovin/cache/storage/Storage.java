package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.core.CacheException;

import java.util.Map;
import java.util.Optional;

/**
 * Interface of a storage to keep data, get it, check is contains
 * @param <K> key
 * @param <V> value
 */
public interface Storage<K, V> {

    Optional<V> put(K key, V value);

    Optional<V> get(K key);

    Optional<V> remove(K key);

    Map<K, V> getAll() throws CacheException;

    boolean containsKey(K key);

    void clear();

    int size();

    boolean isEmpty();
}
