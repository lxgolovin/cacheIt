package com.lxgolovin.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedLRU<K> extends LinkedHashMap implements CacheAlgorithm<K>  {

    private Map lruMap;
/*
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {

        return super.removeEldestEntry(eldest);
    }
*/


    public LinkedLRU() {
        lruMap = new LinkedHashMap();
    }

    public LinkedLRU(K key) {
        lruMap = new LinkedHashMap();
        add(key);
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
    public K add(K key) {
        return null;
    }

    @Override
    public K renew(K key) {
        return null;
    }

    @Override
    public K delete(K key) {
        return null;
    }
}
