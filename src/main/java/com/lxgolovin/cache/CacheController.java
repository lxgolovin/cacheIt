package com.lxgolovin.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @param <K>
 * @param <V>
 */
public class CacheController<K,V> {

    /**
     *
     */
    private LinkedList<Cache<K,V>> cclist = new LinkedList<>();

    /**
     *
     */
    public CacheController() {
        CacheAlgorithm<K> lru = new LruMru<>();
        Cache<K, V> cache = new MemoryCache<>(lru);
        addLevel(cache);
    }

    /**
     *
     * @param cache
     * @return
     */
    public int addLevel(Cache<K,V> cache) {
        cclist.add(cache);
        return cclist.size();
    }

    /**
     *
     * @param index
     */
    public void removeLevel(int index) {
        //TODO: possibly need to clean cache and move data to next levels
        cclist.remove(index);
    }

    /**
     *
     * @return
     */
    public int levels() {
        return cclist.size();
    }

    private boolean isCcEmpty() {
        return levels() < 1;
    }

    /**
     * @param key if null, returns false
     * @param value if null, returns false
     * @return returns true if success, else false
     * @throws NoSuchElementException generates when trying to add data to empty cache controller
     * @throws IllegalArgumentException if any of the params is null
     */
    public K load(K key, V value) {
        K retKey;

        if (isCcEmpty()) {
            throw new NoSuchElementException();
        }
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }

        retKey = cclist.getLast().cache(key, value);
        // TODO: need to implement move to next level
        return (key.equals(retKey)) ? key : retKey;
    }

    /**
     *
     * @return
     */
    public List<K> getData() {
        //TODO: export all data to List
        return null;
    }
}
