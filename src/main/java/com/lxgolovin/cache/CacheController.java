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
        Cache<K, V> cacheLevel = (cache == null) ? createNewMemoryCacheLru() : cache;
        addLevel(cacheLevel);
    }

    private SwCache<K, V> createNewMemoryCacheLru() {
        return new SwCache<>(new Lru<>());
    }

    /**
     * Used to add levels to Cache controller, increasing the size of {@link CacheController#ccList}
     * @param cache new cache level with predefined algorithm
     * @return the number of levels after adding
     */
    public int addLevel(Cache<K, V> cache) {
        Cache<K, V> cacheLevel = (cache == null) ? createNewMemoryCacheLru() : cache;
        ccList.add(cacheLevel);
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
     * @param key cannot be null
     * @param value cannot be null
     * @throws IllegalArgumentException if input parameters are null
     */
    public Optional<Map.Entry<K, V>> cache(K key, V value) {
        // TODO: need refactoring
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }

        // if key is already in cache, getLevel(key) finds it
        int startIndex = getLevelByKey(key);
        if (startIndex == levels()) {
            // the key is not in cache, so start inserting the key-value from 0 level
            startIndex = 0;
        }

        return loadToLevel(key, value, startIndex);
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
    private Optional<Map.Entry<K, V>> loadToLevel(K key, V value, int index) {
        // TODO: need refactoring
        /*
        Map.Entry<K, V> returnEntry = ccList.get(index).cache(key, value).orElse(null);
        if (returnEntry == null){
            return Optional.empty();
        }

        if ((key != returnEntry.getKey()) && (levels() > (++index))) {
            // one more recursive if some entry popped out and there are still more levels
            return loadToLevel(returnEntry.getKey(), returnEntry.getValue(), index);
        }

        return Optional.of(returnEntry);

         */

        Optional<Map.Entry<K, V>> returnEntry = ccList.get(index).cache(key, value);

        int nextLevel = ++index;
//        if (returnEntry.isPresent()) {
            return returnEntry
                    .filter(e -> ((e.getKey()!=key) && (levels() > nextLevel)))
                    .map(e -> {
                        Optional<Map.Entry<K, V>> newE = loadToLevel(e.getKey(), e.getValue(), nextLevel);
                        return newE;
                    }).orElse(returnEntry);

//        }

//        if ((key != returnEntry.getKey()) && (levels() > (++index))) {
            // one more recursive if some entry popped out and there are still more levels
//            return loadToLevel(returnEntry.getKey(), returnEntry.getValue(), index);
//        }

//        return returnEntry;
    }

    public Optional<V> get(K key) {
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
        // TODO: need refactoring
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
        return loadToLevel(popped.getKey(), popped.getValue(), startIndex);
    }

    @Override
    public Optional<V> delete(K key) {
        // TODO: need to ask Mike, how to simplify
        return ccList.stream()
                .filter(c -> c.contains(key))
                .findAny()
                .map(c -> c.delete(key).orElse(null));
//                .orElse(Optional.empty());
    }

    @Override
    public boolean contains(K key) {
        return ccList.stream().anyMatch(c -> c.contains(key));
    }

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

    private int getLevelByKey(K key) {
        return IntStream.range(0, levels())
                .filter(i -> (ccList.get(i).contains(key)))
                .findAny()
                .orElse(levels());
    }
}
