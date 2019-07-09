package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.algorithm.Mru;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Cache controller creates several levels of cache with different algorithms to get
 * possibility to keep data. Cache controller implements {@link Cache} and is done as a
 * list of cached levels
 * As levels different types of caches could be used, e.g. {@link SwCache}
 * As algorithms {@link CacheAlgorithm}
 * @param <K> to keep keys
 * @param <V> to keep values
 * @see Cache
 * @see CacheAlgorithm
 * @see SwCache
 * @see Lru
 * @see Mru
 */
public class CacheController<K, V> implements Cache<K,V> {

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
     * @throws IndexOutOfBoundsException if there is now level with such index
     */
    public boolean isLevelFull(int index) {
        if ((index < 0) | (index >= levels())) {
            throw new IndexOutOfBoundsException();
        }

        return (ccList.get(index).size() == ccList.get(index).sizeMax());
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
    public Optional<Map.Entry<K, V>> cache(K key, V value) {
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }

        // if key is already in cache, getLevel(key) finds it
        int startIndex = getLevel(key);
        if (startIndex == levels()) {
            // the key is not in cache, so start inserting the key-value from 0 level
            startIndex = 0;
        }

//        return load(key, value, startIndex);
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
    private Optional<Map.Entry<K, V>> load(K key, V value, int index) {
//        return ccList.get(index).cache(key, value).map(entry -> {
//            if ((key != entry.getKey()) && (levels() > (++index))) {
//                 one more recursive if some entry popped out and there are still more levels
//                return load(entry.getKey(), entry.getValue(), index);
//            }
//        });
        Map.Entry<K, V> returnEntry = ccList.get(index).cache(key, value).orElse(null);
        if (returnEntry == null){
            return Optional.empty();
        }

        if ((key != returnEntry.getKey()) && (levels() > (++index))) {
            // one more recursive if some entry popped out and there are still more levels
            return load(returnEntry.getKey(), returnEntry.getValue(), index);
        }

        return Optional.of(returnEntry);
    }

    /**
     * Gets an entry by the key from cache. Searches in all levels
     * @param key with mapping in cache to value
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return ccList.stream()
                .filter(c -> c.contains(key))
                .findAny()
                .map(c -> c.get(key).orElse(null));
//                .orElse(Optional.empty());
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * In multilevel cache the value is popped from the first level, then trying
     * to insert it in next level.
     * @return popped out entry if it was the last available level.
     *          Returns null entry if the element was not found in algorithm queue (empty) or
     *          if there are no levels or all levels are empty.
     *          Returns replaced entry if the popped one from first levels moved to next level
     */
    @Override
    public Optional<Map.Entry<K, V>> pop() {
        // check if there are no levels or all levels are empty
        if ((levels() < 1) || (size() < 1)) {
            return Optional.empty();
        }

        // try to pop from first levels. One by one. If first is empty, try next
        int startIndex = IntStream.range(0, levels())
                .filter(i -> (ccList.get(i).size() > 0))
                .findFirst()
                .orElse(0);

        // found first not empty level. key-value are popped and now try to insert into
        // "startIndex" level
        Map.Entry<K, V> popped = ccList.get(startIndex).pop().orElse(null);
        startIndex++;

        if (popped == null) {
            return Optional.empty();
        }
        // enter recursive method to insert popped key-value
        return load(popped.getKey(), popped.getValue(), startIndex);
    }

    /**
     * Removes the mapping for a key from this cache. Does not depend on algorithm type
     *
     * <p>Returns the value for the associated key,
     * or <tt>null</tt> if the cache contained no mapping for the key.
     *
     * @param key key whose mapping is to be removed from the cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws IllegalArgumentException if key is null
     */
    @Override
    public Optional<V> delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        // TODO: need to ask Mike, how to simplify
        return ccList.stream()
                .filter(c -> c.contains(key))
                .findAny()
                .map(c -> c.delete(key).orElse(null));
//                .orElse(Optional.empty());
    }

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false
     * @throws IllegalArgumentException if key is null
     */
    @Override
    public boolean contains(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return ccList.stream().anyMatch(c -> c.contains(key));
    }

    /**
     * Clears all data from the queue
     * All elements are deleted.
     */
    @Override
    public void clear() {
        ccList.forEach(Cache::clear);
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return ccList.stream().mapToInt(Cache::size).sum();
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return ccList.stream().mapToInt(Cache::sizeMax).sum();
    }

    /**
     * Debug method for internal use only
     * @param key to get level
     * @return index of level if the key is present,
     *          else number of levels
     */
    private int getLevel(K key) {
        return IntStream.range(0, levels())
                .filter(i -> (ccList.get(i).contains(key)))
                .findAny()
                .orElse(levels());
    }
}
