package com.lxgolovin.cache;

// TODO: To be documented
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @param <K>
 * @param <V>
 */
public class MemoryCache<K, V> implements Cache<K, V>  {

    /**
     * Default cache size
     */
    static final int DEFAULT_CACHE_SIZE = 5;

    /**
     * Map to keep data
     */
    private Map<K, V> cacheMap;

    /**
     *
     */
    private CacheAlgorithm<K> algo;

    /**
     *
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm) {
        cacheMap = new HashMap<>();
        algo = algorithm;
    }

    /**
     *
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, K key, V value) {
        cacheMap = new HashMap<>();
        algo = algorithm;
        cache(key, value);
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public K cache(K key, V value) {
        if ((key == null) | (value == null) ) {
            throw new IllegalArgumentException();
        }

        // TODO: need to implement dynamic size change during init phase
        if (size() == DEFAULT_CACHE_SIZE) {
            // using deletion by algorithm
            delete();
        }
        cacheMap.put(algo.shift(key), value);
        return key;
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public V get(K key){
        if ((key == null) || (!cacheMap.containsKey(key))) {
            throw new IllegalArgumentException();
        }
        return cacheMap.get(algo.shift(key));
    }

    /**
     *
     * @return
     */
    @Override
    public K delete() {
        return delete(algo.delete());
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public K delete(K key) {
        if ((key == null) || (!cacheMap.containsKey(key))) {
            throw new IllegalArgumentException();
        }
        cacheMap.remove(algo.delete(key));
        return key;
    }

    /**
     *
     */
    @Override
    public void clear(){
        cacheMap.clear();
        algo.flash();
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return cacheMap.size();
    }
}