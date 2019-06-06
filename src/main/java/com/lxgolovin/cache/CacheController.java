package com.lxgolovin.cache;

import java.util.*;

/**
 * Cache controller creates several levels of cache with different algorithms to get
 * possibility to keep data. Cache controller implements {@link Cache} and is done as a
 * list of cached levels
 * As levels different types of caches could be used, e.g. {@link MemoryCache}
 * As algorithms {@link CacheAlgorithm}
 * @param <K> to keep keys
 * @param <V> to keep values
 * @see Cache
 * @see CacheAlgorithm
 * @see MemoryCache
 * @see LruAlgorithm
 * @see MruAlgorithm
 */
public class CacheController<K, V> implements Cache<K, V> {

    /**
     * Cache controller list to keep levels of cache
     */
    private final List<Cache<K, V>> ccList = new LinkedList<>();

    /**
     * Constructor for the cache controller. Add first level by default
     * @param cache level with specified algorithm
     */
    public CacheController(Cache<K, V> cache) {
        addLevel(cache);
    }

    /**
     * Used to add levels to Cache controller, increasing the size of {@link CacheController#ccList}
     * @param cache new cache level with predefined algorithm
     * @return the number of levels after adding
     */
    public int addLevel(Cache<K, V> cache) {
        ccList.add(cache);
        return levels();
    }

    /**
     * Removes levels in cached controller
     * @param index of the level to be removed
     * @return the number of levels after removing
     * @throws IndexOutOfBoundsException if there is now level with such index
     */
    public int removeLevel(int index) {
        //TODO: possibly need to move data to next levels when deleting level
        if ((index < 0) | (index >= levels())) {
            throw new IndexOutOfBoundsException();
        }

        ccList.get(index).clear();
        ccList.remove(index);
        return levels();
    }

    /**
     * Gets number of cache levels
     * @return number of levels
     */
    public int levels() {
        return ccList.size();
    }

    /**
     * Checks if the level is full up or not
     * @param index of the level
     * @return true if full up, else false
     */
    public boolean isLevelFull(int index) {
        return (ccList.get(index).size() == ccList.get(index).sizeMax());
    }

    /**
     * Checks if cache controller is empty or not
     * @return true if not empty, else false
     */
    private boolean isCcEmpty() {
        return levels() < 1;
    }

     /**
     * Caches data into cache by key value. If cache is full up, data is removed (popped out) from
     * cache using some algorithm
     * @param key to define data to be loaded to cache
     * @param value to be loaded to cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         If any key-value mapping was popped during this task, because of size limit,
     *         the deleted key-value mapping will be returned.
     * @throws IllegalArgumentException if input parameters are null
     */
    public Map.Entry<K, V> cache(K key, V value) {
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }

        // if key is already in cache, getLevel(key) finds it
        int startIndex = getLevel(key);
        if (startIndex == levels()) {
            // the key is not in cache, so start inserting the key-value from 0 level
            startIndex = 0;
        }

        return load(key, value, startIndex);
    }

    /**
     * Loads new data (key and value) in recursive way.
     * Goes through all levels and moves data (popped out or inserted)
     * @param key for the data
     * @param value data
     * @param index for the level to insert values
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         If any key-value mapping was popped during this task, because of size limit,
     *         the deleted key-value mapping will be returned.
     */
    private Map.Entry<K, V> load(K key, V value, int index) {
        Map.Entry<K, V> returnEntry = ccList.get(index).cache(key, value);

        if ((key != returnEntry.getKey()) && (levels() > (++index))) {
            // one more recursive if some entry popped out and there are still more levels
            return load(returnEntry.getKey(), returnEntry.getValue(), index);
        }

        return returnEntry;
    }



    /**
     * Gets an entry by the key from cache. Searches in all levels
     * @param key with mapping in cache to value
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    public V get(K key) {
        // TODO: index can be implemented for searching
        for (Cache<K,V> c: ccList) {
            if (c.contains(key)) {
                return c.get(key);
            }
        }
        return null;
    }

    // TODO: Staring from this line, class is not documented. In progress
    /*
    NOT implemented yet
     */
    @Override
    public Map.Entry<K, V> pop() {
        return null;
    }

    /*
    NOT implemented yet
     */
    @Override
    public V delete(K key) {
        return null;
    }

    /*
    NOT implemented yet
     */
    @Override
    public boolean contains(K key) {
        return false;
    }

    /*
    NOT implemented yet
     */
    @Override
    public void clear() {
    }

    /*
    NOT implemented yet
    */
    @Override
    public int size() {
        return 0;
    }

    /*
    NOT implemented yet
     */
    @Override
    public int sizeMax() {
        return 0;
    }

    /**
     * Debug method for internal use only
     * @param key to get level
     * @return index of level if the key is present,
     *          else number of levels
     */
    int getLevel(K key) {
        for (int i = 0; i < levels(); i++) {
            if (ccList.get(i).contains(key)) {
                return i;
            }
        }
        return levels();
    }
}
