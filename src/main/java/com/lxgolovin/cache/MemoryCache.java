package com.lxgolovin.cache;

// TODO: To be documented
import java.util.AbstractMap;
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
     * @param entry with data to be loaded to cache
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, AbstractMap.SimpleEntry<K,V> entry) {
        cacheMap = new HashMap<>();
        algo = algorithm;
        cache(entry);
    }

    /**
     *
     * @param algorithm
     * @param key
     * @param value
     */
    public MemoryCache(CacheAlgorithm<K> algorithm, K key, V value) {
        this(algorithm, new AbstractMap.SimpleEntry<>(key, value));
    }

    /**
     * @param key - may not be null
     * @param value - may no be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> cache(K key, V value) {
        return cache(new AbstractMap.SimpleEntry<>(key, value));
//        AbstractMap.SimpleEntry<K,V> result = new AbstractMap.SimpleEntry<>(key, value);
//        K result = key;

//        if ((key == null) | (value == null) ) {
//            throw new IllegalArgumentException();
//        }
//
//         TODO: need to implement dynamic size change during init phase
//        if (size() == DEFAULT_CACHE_SIZE) {
//             using deletion by algorithm
//            result = delete();
//        }
//        cacheMap.put(algo.shift(key), value);
//        return result;
    }

    /**
     *
     * @param entry
     * @return
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> cache(AbstractMap.SimpleEntry<K, V> entry) {
        AbstractMap.SimpleEntry<K,V> result = entry;

        if ((entry == null) || (entry.getKey() == null) || (entry.getValue() == null) ) {
            throw new IllegalArgumentException();
        }

        // TODO: need to implement dynamic size change during init phase
        if (size() == DEFAULT_CACHE_SIZE) {
            // using deletion by algorithm
            result = delete();
        }

        algo.shift(entry.getKey());
        cacheMap.put(entry.getKey(), entry.getValue());
        return result;
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public AbstractMap.SimpleEntry<K,V> get(K key){
        if ((key == null) || (!cacheMap.containsKey(key))) {
            throw new IllegalArgumentException();
        }
        return  new AbstractMap.SimpleEntry<>(key, cacheMap.get(algo.shift(key)));
    }

    /**
     *
     * @return
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> delete() {
        return delete(get(algo.delete()));
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public AbstractMap.SimpleEntry<K,V> delete(K key) {
        return delete(new AbstractMap.SimpleEntry<>(get(key)));
    }

    /**
     *
     * @param entry
     * @return
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> delete(AbstractMap.SimpleEntry<K, V> entry) {
        K key = entry.getKey();
        if ((key == null) || (!cacheMap.containsKey(key))) {
            throw new IllegalArgumentException();
        }

        cacheMap.remove(algo.delete(key));
        return entry;
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