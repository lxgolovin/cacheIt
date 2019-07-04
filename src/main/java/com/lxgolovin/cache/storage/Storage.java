package com.lxgolovin.cache.storage;

public interface Storage<K, V> {


    V put(K key, V value);
    V get(K key);
    boolean containsKey(K key);
    V remove(K key);
    void clear();
    int size();
}
