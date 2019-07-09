package com.lxgolovin.cache.storage;

import java.util.Map;
import java.util.Optional;
// TODO: need some comments what is it
public interface Storage<K, V> {

    Optional<V> put(K key, V value);

    Optional<V> get(K key);

    Optional<V> remove(K key);

    Map<K, V> getAll();

    boolean containsKey(K key);

    void clear();

    int size();

    boolean isEmpty();
}
