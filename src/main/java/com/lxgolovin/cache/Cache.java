package com.lxgolovin.cache;
// TODO: To be documented
public interface Cache<K, V> {
    K cache(K key, V value);
    V get(K key);
    K delete();
    K delete(K key);
    void clear();
    int size();
}
