package main.java.com.lxgolovin.cacheit;

import java.util.concurrent.ConcurrentHashMap;

public class CacheItInMemory<K, V> implements Cache<K, V> {

    private ConcurrentHashMap<K, V> cacheMap;
    // private Integer maxCacheSize = 5;


    public CacheItInMemory() {
        this.cacheMap = new ConcurrentHashMap<>();
    }

    public CacheItInMemory(K key, V value) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.cacheMe(key, value);
    }



    // public Integer getCacheSize() {
    //     return maxCacheSize;
    //}

    //public void setCacheSize(Integer maxCacheSize) {
    //    this.maxCacheSize = maxCacheSize;
    //}

    @Override
    public V getValue(K key){
        if ( key != null && cacheMap.containsKey(key) ) {
            return cacheMap.get(key);
        }
        return null;
    }

    @Override
    public K cacheMe(K key, V value) {
        // cacheMap.putIfAbsent(key,value);
        try {
            cacheMap.put(key, value);
        } catch (NullPointerException e) {
            return null;
        }
        return key;
    }

    @Override
    public K deleteValue(K key) {
        if ( key != null ) {
            cacheMap.remove(key);
        }
        return key;
    }

    @Override
    public void clearAll(){
        cacheMap.clear();
    }

    @Override
    public Integer cacheSize() {
        return cacheMap.size();
    }

}
