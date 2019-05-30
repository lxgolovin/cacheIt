package com.lxgolovin.cache;

public interface CacheAlgo<K, V> {
    K head();
    K tail();
    K add(K key, V value);
    K renew(K key, V value);
    K del(K key);
}
