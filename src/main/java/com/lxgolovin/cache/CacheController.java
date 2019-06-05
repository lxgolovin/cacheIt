package com.lxgolovin.cache;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Cache controller creates several levels of cache with different algorithms to get
 * pssibility to keep data
 * @param <K> to keep keys
 * @param <V> to keep values
 */
public class CacheController<K,V> {

    /**
     * Cache controller list to keep levels of cache
     */
    private LinkedList<Cache<K,V>> ccList = new LinkedList<>();

    /**
     * Constructor for the cache controller. Add first level by default
     */
    public CacheController(Cache<K,V> cache) {
        addLevel(cache);
    }

    /**
     * Used to add levels to Cache controller, increasing the size of {@link CacheController#ccList}
     * @param cache new cache level with predefined algorithm
     * @return the number of levels after adding
     */
    public int addLevel(Cache<K,V> cache) {
        ccList.add(cache);
        return levels();
    }

    /**
     * Removes levels in cached controller
     * @param index of the level to be removed
     * @return the number of levels after adding
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
     * Checks if the level is full up or not
     * @param index of the level
     * @return true if full up, else false
     */
    public boolean isLevelFull(int index) {
        return (ccList.get(index).size() == ccList.get(index).sizeMax());
    }

    /**
     * Gets number of cache levels
     * @return number of levels
     */
    public int levels() {
        return ccList.size();
    }

    /**
     * Checks if cache controller is empty or not
     * @return true if not empty, else false
     */
    private boolean isCcEmpty() {
        return levels() < 1;
    }

    /**
     * Loads new data (key and value)
     * @param key for the data
     * @param value data
     * @return entry with (key and value) that were deleted or
     *          entry with input data
     * @throws IllegalArgumentException if input parameters are null
     */
    public AbstractMap.SimpleEntry<K,V> load(K key, V value) {
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }
        return load(new AbstractMap.SimpleEntry<>(key, value));
    }

    /**
     * Loads new data (key and value)
     * @param entry with data
     * @return entry with (key and value) that were deleted or
     *          entry with input data
     * @throws IllegalArgumentException if input parameters are null
     * @throws NoSuchElementException if no level available
     */
    public AbstractMap.SimpleEntry<K,V> load(AbstractMap.SimpleEntry<K,V> entry) {
        int index = 0;
        if (isCcEmpty()) {
            throw new NoSuchElementException();
        }
        if ((entry == null)) {
            throw new IllegalArgumentException();
        }
        return load(entry, index);
    }

    /**
     * Loads new data (key and value) in recursive way
     * @param entry with data
     * @param index for the level to insert values
     * @return entry with (key and value) that were deleted or
     *          entry with input data
     */
    private AbstractMap.SimpleEntry<K,V> load(AbstractMap.SimpleEntry<K,V> entry, int index) {
        AbstractMap.SimpleEntry<K,V> entryBuffer = ccList.get(index).cache(entry);
        return ((entry.getKey() != entryBuffer.getKey()) && (levels() > (++index))) ?
                load(entryBuffer, index) : entryBuffer;
    }

    /**
     * Same as {@link CacheController#get(Object)}, but with iterator
     * Gets an entry by the key from cache. Searches in all levels
     * @param key to get entry
     * @return null key not found
     */
    public AbstractMap.SimpleEntry<K,V> getIter(K key) {
        Iterator<Cache<K,V>> iterator = ccList.iterator();
        while (iterator.hasNext()) {
            Cache<K,V> c = iterator.next();
            if (c.contains(key)) {
                return c.get(key);
            }
        }
        return null;
    }

    /**
     * Gets an entry by the key from cache. Searches in all levels
     * @param key to get entry
     * @return null key not found
     */
    public AbstractMap.SimpleEntry<K,V> get(K key) {
        for (Cache<K,V> c: ccList) {
            if (c.contains(key)) {
                return c.get(key);
            }
        }
        return null;
    }

    /**
     * debug method. Is used for internal tests only
     * @param key to get data
     * @param index of cache level
     * @return entry with data
     * @throws IllegalArgumentException if key is null
     */
    AbstractMap.SimpleEntry<K,V> get(K key, int index) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return ccList.get(index).get(key);
    }

    /**
     * Debug method for internal use only
     * @param key to get level
     * @return index of level
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
