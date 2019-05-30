package com.lxgolovin.cache;

import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;

public class MemoryCache<K, V> implements Cache<K, V>, FrequencyCount<K> {

    // private ConcurrentHashMap<K, V> cacheMap;
    private HashMap<K, V> cacheMap;
    private HashMap<K,Integer> freqMap;


    public MemoryCache() {
        // this.cacheMap = new ConcurrentHashMap<>();
        this.cacheMap = new HashMap<>();
        this.freqMap = new HashMap<>();
    }

    public MemoryCache(K key, V value) {
        // this.cacheMap = new ConcurrentHashMap<>();
        this.cacheMap = new HashMap<>();
        this.freqMap = new HashMap<>();
        this.cache(key, value);
        this.freqMap.put(key, 1);
    }


    @Override
    public V get(K key){
        if ( key != null && cacheMap.containsKey(key) && freqMap.containsKey(key) ) {
//            freqMap
            return cacheMap.get(key);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public K cache(K key, V value) {
        // cacheMap.putIfAbsent(key,value);
        if ( key != null & value != null ) {
            cacheMap.put(key, value);
            return key;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public K delete(K key) {
        if ( key != null ) {
            cacheMap.remove(key);
            return key;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void clear(){
        cacheMap.clear();
    }

    @Override
    public int size() {
        return cacheMap.size();
    }


    public int frequency(K key){
        return 0;
    }


    public K mostUsed() {
        return null;
    }

    public K leastUsed(){
        return null;
    }

}
