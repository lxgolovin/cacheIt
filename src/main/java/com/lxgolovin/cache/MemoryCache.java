package main.java.com.lxgolovin.cache;

import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;

public class MemoryCache<K, V> implements Cache<K, V> {

    // private ConcurrentHashMap<K, V> cacheMap;
    private HashMap<K, V> cacheMap;


    public MemoryCache() {
        // this.cacheMap = new ConcurrentHashMap<>();
        this.cacheMap = new HashMap<>();
    }

    public MemoryCache(K key, V value) {
        // this.cacheMap = new ConcurrentHashMap<>();
        this.cacheMap = new HashMap<>();
        this.cache(key, value);
    }


    @Override
    public V get(K key){
        if ( key != null && cacheMap.containsKey(key) ) {
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

}
