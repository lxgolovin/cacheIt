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
    //! final
    private final Map<K, V> cacheMap;

    /**
     *
     */
    //! final
    private CacheAlgorithm<K> algo;

    /**
     *
     * @param algorithm specifies algorithm type that is used by the cache
     */

    //! add one more constructor with Map as parameter.

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
     * @param algorithm specifies algorithm type that is used by the cache
     * @param key specifies key for the entry
     * @param value defined value inside entry
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
        AbstractMap.SimpleEntry<K,V> result = new AbstractMap.SimpleEntry<>(key, value);
        //! This should be the first line. Validation first.
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        // TODO: need to implement dynamic size change during init phase
        //! Move a size param to constructors
        if (size() == DEFAULT_CACHE_SIZE & !contains(key)) {
            // using deletion by algorithm
            result = delete();
        }

        cacheMap.put(algo.shift(key), value); //! Do not mix up the logic in one line
        return result;
    }

    /**
     *
     * @param entry with data to be loaded to cache
     * @return deleted entry if cache was full, else the entry that was loaded
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> cache(AbstractMap.SimpleEntry<K, V> entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        return cache(entry.getKey(), entry.getValue());
    }

    /**
     * @param key - may not be null
     * @return the entry by the key
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public AbstractMap.SimpleEntry<K,V> get(K key){
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return  (contains(key)) ?
                new AbstractMap.SimpleEntry<>(key, cacheMap.get(algo.shift(key))) : null; //! several lines
    }

    /**
     * Checks if the key is present in cache
     * @param key to check in cache
     * @return true is element found, else false
     */
    @Override
    public boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

    /**
     *
     * @return entry deleted by the defined algorithm
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> delete() {
        return delete(get(algo.delete()));
    }

    /**
     * @param key - may not be null
     * @return entry deleted by the key
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link MemoryCache#cacheMap} does not contain the key
     */
    @Override
    public AbstractMap.SimpleEntry<K,V> delete(K key) {
//        if ((key == null) || (!cacheMap.containsKey(key))) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        return  (contains(key)) ?
                new AbstractMap.SimpleEntry<>(key, cacheMap.remove(algo.delete(key))) : null;
//        return new AbstractMap.SimpleEntry<>(key, cacheMap.remove(algo.delete(key)));
    }

    /**
     *
     * @param entry to be deleted, cannot be null (note!: the entry is deleted only by key)
     *              using {@link MemoryCache#delete(K key)} method, no checks for value=value done
     * @return entry deleted by the key
     * @throws IllegalArgumentException if any of the params is null or
     */
    @Override
    public AbstractMap.SimpleEntry<K, V> delete(AbstractMap.SimpleEntry<K, V> entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        return delete(entry.getKey());
    }

    /**
     *
     */
    @Override
    public void clear(){
        cacheMap.clear(); //! Why is it here clear() and in the next line flash()
        algo.flash();
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return cacheMap.size();
    }

    /**
     * @return cache maxsize
     */
    @Override
    public int sizeMax() {
        // TODO: when size change will be implemented, this method to be fixed
        return DEFAULT_CACHE_SIZE;
    }
}