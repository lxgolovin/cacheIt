package com.lxgolovin.cache;

public interface Cache<K, V> {
    K cache(K key, V value);
    V get(K key);
    K del(K key);
    void clear();
    int size();
}
