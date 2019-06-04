package com.lxgolovin.cache;

import java.util.AbstractMap;
import java.util.LinkedList;
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
    private LinkedList<Cache<K,V>> ccList = new LinkedList<>();

    /**
     *
     */
    public CacheController(Cache<K,V> cache) {
        addLevel(cache);
    }

    /**
     *
     * @param cache level
     * @return the number of levels
     */
    public int addLevel(Cache<K,V> cache) {
        ccList.add(cache);
        return levels();
    }

    /**
     *
     * @param index
     */
    public void removeLevel(int index) {
        //TODO: possibly need to clean cache and move data to next levels
        ccList.get(index).clear();
        ccList.remove(index);
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
     *
     * @return
     */
    public int levels() {
        return ccList.size();
    }

    /**
     *
     * @return
     */
    private boolean isCcEmpty() {
        return levels() < 1;
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public AbstractMap.SimpleEntry<K,V> load(K key, V value) {
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }
        return load(new AbstractMap.SimpleEntry<>(key, value));
    }

    /**
     *
     * @param entry
     * @return
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
     *
     * @param entry
     * @param index
     * @return
     */
    private AbstractMap.SimpleEntry<K,V> load(AbstractMap.SimpleEntry<K,V> entry, int index) {
        AbstractMap.SimpleEntry<K,V> entryBuffer = ccList.get(index).cache(entry);
        return ((entry != entryBuffer) && (levels() > (++index))) ?
                load(entryBuffer, index) : entryBuffer;
    }

    /**
     * debug method. Will be corrected to get value only by key
     * @param key
     * @param index
     * @return
     */
    public AbstractMap.SimpleEntry<K,V> get(K key, int index) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return ccList.get(index).get(key);
    }
}
