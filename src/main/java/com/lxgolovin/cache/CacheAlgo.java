package com.lxgolovin.cache;

public interface CacheAlgo<K> {
    K head();
    K tail();
    K add(K key);
    K renew(K key);
    K del(K key);
}
