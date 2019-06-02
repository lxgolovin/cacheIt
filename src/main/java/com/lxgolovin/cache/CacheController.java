package com.lxgolovin.cache;

import java.util.LinkedList;
import java.util.List;

public class CacheController<K,V> {

    private LinkedList<Cache> cclist = new LinkedList<>();

    public CacheController() {
        CacheAlgorithm<K> lru = new LruMru<>();
        Cache<K, V> cache = new MemoryCache<>(lru);
        addLevel(cache);
    }

    public int addLevel(Cache<K,V> cache) {
        cclist.add(cache);
        return cclist.size();
    }

    public void removeLevel(int index) {
        //TODO: possibly need to clean cache and move data to next levels
        cclist.remove(index);
    }

    public int levels() {
        return cclist.size();
    }

    /**
     * @param key if null, returns false
     * @param value if null, returns false
     * @return returns true if success, else false
     */
    public boolean load(K key, V value) {
        if ( key == null | value == null ) { return false; }
        return true;
    }

    public List<K> getData() {
        //TODO: export all data to List
        return null;
    }
}
