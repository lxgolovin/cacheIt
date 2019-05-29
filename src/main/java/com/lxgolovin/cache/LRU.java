package main.java.com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class LRU<K, V> implements CacheAlgo<K,V> {

    private Map lruMap;

    public LRU() {
        this.lruMap = new HashMap<>();
    }

    public LRU(K key, V value) {
        this.lruMap = new HashMap<>();
        this.add(key, value);
    }

    @Override
    public K head() {
        return null;
    }

    @Override
    public K tail() {
        return null;
    }

    @Override
    public K renew(K key, V value) {
        return null;
    }

    @Override
    public K add(K key, V value) {
        return null;
    }

    @Override
    public K del(K key, V value) {
        return null;
    }
}
