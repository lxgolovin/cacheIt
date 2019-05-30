package com.lxgolovin.cache;

public interface CacheAlgorithm<K> {
    K head();
    K tail();
    K add(K key);
    K renew(K key);
    K delete(K key);
}
