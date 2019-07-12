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
 * As levels different types of caches could be used, e.g. {@link CacheLevel}
 * As algorithms {@link CacheAlgorithm}
 * @param <K> to keep keys
 * @param <V> to keep values
 * @see Cache
 * @see CacheAlgorithm
 * @see CacheLevel
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

    private CacheLevel<K, V> createNewMemoryCacheLru() {
        return new CacheLevel<>(new Lru<>());
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
        if ((key == null) | (value == null)) {
            throw new IllegalArgumentException();
        }

        int levelIndex = (contains(key)) ? getLevelByKey(key) : 0;
        return loadToLevel(key, value, levelIndex);
    }

    /**
     * Loads new data (key and value) in recursive way.
     * Goes through all levels and moves data (popped out or inserted)
     */
    private Optional<Map.Entry<K, V>> loadToLevel(K key, V value, int index) {
         Optional<Map.Entry<K, V>> returnEntry = ccList.get(index).cache(key, value);

        int nextLevel = index + 1;
        return returnEntry
                .filter(e -> ((e.getKey()!=key) && (levels() > nextLevel)))
                .map(e -> loadToLevel(e.getKey(), e.getValue(), nextLevel))
                .orElse(returnEntry);
    }

    public Optional<V> get(K key) {
        return ccList.stream()
                .filter(c -> c.contains(key))
                .findAny()
                .flatMap(c -> c.get(key))
                .flatMap(v -> delete(key))
                .map(v -> {
                    cache(key, v);
                    return v;
                });
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * In multilevel cache the value is popped from the first level, then trying
     * to insert it in next level.
     */
    @Override
    public Optional<Map.Entry<K, V>> pop() {
        // check if there are no levels or all levels are empty
        if ((levels() < 1) || (size() < 1)) {
            return Optional.empty();
        }

        // try to pop from first levels. One by one. If first is empty, try next
        int notEmptyLevelIndex = IntStream.range(0, levels())
                .filter(i -> (ccList.get(i).size() > 0))
                .findFirst()
                .orElse(0);
        int nextLevel = notEmptyLevelIndex + 1;

        return ccList
                .get(notEmptyLevelIndex)
                .pop()
                .flatMap(e -> loadToLevel(e.getKey(), e.getValue(), nextLevel));
    }

    @Override
    public Optional<V> delete(K key) {
        return ccList.stream()
                .filter(c -> c.contains(key))
                .findAny()
                .flatMap(c -> c.delete(key));
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
