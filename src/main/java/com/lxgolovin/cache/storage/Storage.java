package com.lxgolovin.cache.storage;

import java.util.Map;

public interface Storage<K, V> {


    V put(K key, V value);
    V get(K key);
    Map<K, V> getAll();
    boolean containsKey(K key);
    V remove(K key);
    void clear();
    int size();
    boolean isEmpty();
}
