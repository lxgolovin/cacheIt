package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.MemoryStorage;
import com.lxgolovin.cache.storage.Storage;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of interface {@link Cache}. This class creates realization of cache.
 * The default implementation is done using {@link Storage}, where data is stored.
 * Values are stored in storage and removed or kept using different algorithms. As algorithms
 * interface {@link CacheAlgorithm} is used. Also default size is set for the cache, but the
 * size could be set by user. Note that size should by greater then 1, as cache with size 1 has no
 * sense.
 * By default the size of the cache is {@link Cache#DEFAULT_CACHE_SIZE} and the implementation is memory
 * cache {@link MemoryStorage}. But these parameters could be defined during initialization phase
 *
 * Here got methods to cache, delete, pop data by key. Has a possibility to clean data,
 * get maximum available size to current size.
 * @param <K>
 * @param <V>
 * @see Cache
 * @see CacheAlgorithm
 * @see Storage
 * @see MemoryStorage
 * @see FileSystemStorage
 */
public class CacheLevel<K, V> implements Cache<K, V> {

    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link Cache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    private final int maxSize;

    /**
     * Defines cache algorithm
     */
    private final CacheAlgorithm<K> algorithm;

    /**
     * Storage to keep key-values
     */
    private final Storage<K, V> storage;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates memory cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public CacheLevel(CacheAlgorithm<K> algorithm) {
        this(algorithm, new MemoryStorage<>(), new HashMap<>(), Cache.DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * If the map is empty, empty cache is created with maxSize {@link Cache#DEFAULT_CACHE_SIZE}.
     * If the map is not empty, cache is created with maxSize equal to the size of the incoming map.
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     */
    public CacheLevel(CacheAlgorithm<K> algorithm, Map<K, V> map) {
        this(algorithm, null, map, Cache.DEFAULT_CACHE_SIZE);
    }

    public CacheLevel(CacheAlgorithm<K> algorithm, Storage<K, V> storage) {
        this(algorithm, storage, null, Cache.DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates memory cache with default size by defined algorithm.
     * Here key-value for the first element are defined
     * @param algorithm specifies algorithm type that is used by the cache
     * @param key specifies key for the entry
     * @param value defined value inside entry
     */
    public CacheLevel(CacheAlgorithm<K> algorithm, K key, V value) {
        this(algorithm, null, null, Cache.DEFAULT_CACHE_SIZE);
        cache(key, value);
    }

    /**
     * Creates memory cache with defined algorithm and size.
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public CacheLevel(CacheAlgorithm<K> algorithm, int size) {
        this(algorithm, null, null, size);
    }

    public CacheLevel(CacheAlgorithm<K> algorithm, Storage<K, V>  storage, int size) {
        this(algorithm, storage, null, size);
    }

    /**
     * Creates memory cache with defined algorithm and size. Fills with map key-values
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     * @param size defining the size for the mapping
     */
    private CacheLevel(CacheAlgorithm<K> algorithm, Storage<K, V> storage, Map<K, V> map, int size) {
        if (algorithm == null) {
            throw new IllegalArgumentException();
        }

        this.algorithm = algorithm;
        this.storage = (storage == null) ? new MemoryStorage<>() : storage;

        Map<K, V> initialDataMap;
        if (map == null) {
            initialDataMap = this.storage.getAll();
        } else {
            initialDataMap = new HashMap<>(map);
            initialDataMap.forEach(this.storage::put);
        }

        if (!initialDataMap.isEmpty()) {
            maxSize = Math.max(initialDataMap.size(), size);
        } else {
            maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
        }
        putAll(initialDataMap);
    }

    /**
     * Put all values of map into cache
     * @param map with key-values
     */
    private void putAll(Map<K, V> map) {
        map.keySet().forEach(algorithm::shift);
    }

    @Override
    public Optional<Map.Entry<K, V>> cache(K key, V value) {
        Optional<Map.Entry<K, V>> poppedEntry = Optional.empty();
        lock.writeLock().lock();
        try {
            if ((size() >= maxSize) && (!contains(key))) {
                // using deletion by algorithm
                while (size() >= maxSize)
                    poppedEntry = pop();
            }

            algorithm.shift(key);
            Optional<Map.Entry<K, V>> replacedEntry = storage.put(key, value)
                    .map(v -> new AbstractMap.SimpleImmutableEntry<>(key, v));
            return (poppedEntry.isPresent()) ? poppedEntry : replacedEntry;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<V> get(K key){
        lock.readLock().lock();
        try {
            algorithm.shift(key);
            return storage.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return true is element found, else false. Returns false if key is null
     */
    @Override
    public boolean contains(K key) {
        lock.readLock().lock();
        try {
            return ((key != null) && storage.containsKey(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Removes the mapping for a key from this cache. Does not depend on algorithm type
     */
    @Override
    public Optional<V> delete(K key) {
        lock.writeLock().lock();
        try {
            algorithm.delete(key);
            return storage.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all data from the queue
     * All elements are deleted. Elements in the algorithm queue are also deleted
     */
    @Override
    public void clear(){
        lock.writeLock().lock();
        try {
            algorithm.clear();
            storage.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     */
    @Override
    public Optional<Map.Entry<K, V>> pop() {
        lock.writeLock().lock();
        try {
            return algorithm
                    .pop()
                    .flatMap(key -> delete(key)
                            .map(value -> new AbstractMap.SimpleImmutableEntry<>(key, value))
                    );
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return maxSize;
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return storage.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}