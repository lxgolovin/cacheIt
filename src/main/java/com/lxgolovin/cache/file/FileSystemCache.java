package com.lxgolovin.cache.file;

import com.lxgolovin.cache.Cache;
import com.lxgolovin.cache.CacheAlgorithm;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of interface {@link Cache}. This class creates realization of file system cache.
 * The implementation is done using {@link HashMap}, where data is stored.
 * Values are stored on file system as temp files and removed or kept using different algorithms. As algorithms
 * interface {@link CacheAlgorithm} is used. Also default size is set for the cache, but the
 * size could be set by user. Note that size should by greater then 1, as cache with size 1 has no
 * sense.
 *
 * Here got methods to cache, delete, pop data by key. Has a possibility to clean data,
 * get maximum available size to current size.
 * @param <K>
 * @param <V>
 * @see Cache
 * @see CacheAlgorithm
 */
public class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V>  {
    // TODO: much code similar to MemoryCache code. Possibly need AbstractCache class to combine
    // TODO: move file handling methods to separate class
    // TODO: K, V should be serializable

    /**
     * Map-index of files to cache data
     */
    private final Map<K, Path> indexMap;

    /**
     * Temporary directory for the cache files
     */
    private final Storage<K, V> fileStorage;

    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link Cache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    private final int maxSize;

    /**
     * Defines cache algorithm
     */
    private final CacheAlgorithm<K> algo;

    /**
     * Creates file system cache with default size by defined algorithm
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm) {
        this(algorithm, DEFAULT_CACHE_SIZE);
    }

    /**
     * Creates file system cache with defined algorithm and fills it with map key-values.
     * If the map is empty, empty cache is created with maxSize {@link Cache#DEFAULT_CACHE_SIZE}.
     * If the map is not empty, cache is created with maxSize equal to the size of the incoming map.
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm, Map<K, V> map) {
        this(algorithm, map, map.size());
    }

    /**
     * Creates file system cache with default size by defined algorithm.
     * Here key-value for the first element are defined
     * @param algorithm specifies algorithm type that is used by the cache
     * @param key specifies key for the entry
     * @param value defined value inside entry
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm, K key, V value) {
        this(algorithm, DEFAULT_CACHE_SIZE);
        cache(key, value);
    }

    /**
     * Creates file system cache with defined algorithm and size.
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm, int size) {
        this(algorithm, new HashMap<>(), size);
    }

    /**
     * Creates file system cache with defined algorithm and size and fills it with map key-values
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @param map incoming with keys-values of empty
     * @param size defining the size for the mapping
     * @throws IllegalAccessError if cannot create temporary directory
     */
    private FileSystemCache(CacheAlgorithm<K> algorithm, Map<K, V> map, int size) {
        maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;
        algo = algorithm;

        fileStorage = new Storage<>();
        indexMap = new HashMap<>();
        putAll(map);
    }

    /**
     * Creates file system cache with defined algorithm and size and fills it with map key-values
     * Minimum size value is greater then 1. If you try to use less then 2,
     * {@link Cache#DEFAULT_CACHE_SIZE} will be used as a size
     * @param algorithm specifies algorithm type that is used by the cache
     * @throws IllegalAccessError if cannot create temporary directory
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm, Path path) {
        algo = algorithm;
        indexMap = new HashMap<>();

        fileStorage = new Storage<>(path);

        List<Storage<K, V>.OutputNode<Path>> listNodesFromFiles = fileStorage.readAllFromDirectory();
        int size = listNodesFromFiles.size();
        maxSize = (size > 1) ? size : DEFAULT_CACHE_SIZE;

        putAll(listNodesFromFiles);

    }

    /**
     * Put all values of map into cache
     * @param map with key-values
     */
    private void putAll(Map<K, V> map) {
        if (!map.isEmpty()) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                this.cache(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Put all values of list into cache.
     * This is a list of {@link Storage.OutputNode}
     * @param list with key-values-paths
     */
    private void putAll(List<Storage<K, V>.OutputNode<Path>> list) {
        if (!list.isEmpty()) {
            for (Storage<K, V>.OutputNode<Path> node : list) {
                indexMap.put(node.getKey(), node.getPath());
                cache(node.getKey(), node.getValue());
            }
        }
    }

    /**
     * Caches data into cache by key value. If cache is full up, data is removed from
     * cache using some algorithm
     * @param key to define data to be loaded to cache
     * @param value to be loaded to cache
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         If any key-value mapping was popped during this task, because of size limit,
     *         the deleted key-value mapping will be returned.
     * @throws IllegalArgumentException if any of incoming parameters are null
     */
    @Override
    public Map.Entry<K, V> cache(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        Map.Entry<K, V> poppedEntry = null;
        if ((size() == maxSize) && (!contains(key))) {
            // using deletion by algorithm
            poppedEntry = pop();
        }

        Map.Entry<K, V> replacedEntry = null;
        Path filePath;
        if (algo.shift(key)) {
            // need to get file, read old value
            filePath = indexMap.get(key);
            replacedEntry = fileStorage.readFromFile(filePath);

        } else {
            filePath = fileStorage.createFile();
        }

        if (needToStoreNewcomer(replacedEntry, value)) {
            fileStorage.writeToFile(key, value, filePath);
            indexMap.put(key, filePath);
        }

        return (poppedEntry == null) ? replacedEntry : poppedEntry;
    }

    /**
     * Checks if new key-value are really new. If they are alreafy present in storage
     * you do not need to update the file storage. This saves time.
     *
     * @param entry entry, that is possibly kept in storage
     * @param newValue to be checked with entry
     * @return true, if need to update storage, else false
     */
    private boolean needToStoreNewcomer(Map.Entry<K, V> entry, V newValue) {
        return ((entry == null) || ((entry.getValue() != null) && (!entry.getValue().equals(newValue))));
    }

    /**
     * Gets value by the key
     * @param key - may not be null
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws IllegalArgumentException if key is null
     */
    @Override
    public V get(K key){
        if (key == null) {
            throw new IllegalArgumentException();
        }

        // Need to move key as it was accessed. If false, return null
        if (!algo.shift(key)) {
            return null;
        }

        Path path = indexMap.get(key);
        Map.Entry<K, V> entry = fileStorage.readFromFile(path);

        return (entry != null) ? entry.getValue() : null;
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

        return indexMap.containsKey(key);
    }

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * To delete {@link Cache#delete(Object)} is used
     * @return popped out entry, returns null entry if the element was not
     *          found in algorithm queue (empty)
     */
    @Override
    public Map.Entry<K, V> pop() {
        K key = algo.pop();
        if (key == null) {
            return null;
        }

        Map.Entry<K, V> entry;
        V value = delete(key);
        entry = new AbstractMap.SimpleImmutableEntry<>(key, value);

        return entry;
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
     * @throws IllegalArgumentException if any of the params is null
     * @throws IllegalAccessError if file was not deleted as not accessible
     */
    @Override
    public V delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        if (!indexMap.containsKey(key)) {
            return null;
        }

        algo.delete(key);
        Path path = indexMap.remove(key);
        Map.Entry<K, V> entry = fileStorage.readFromFile(path);
        fileStorage.deleteFile(path);

        return (entry != null) ? entry.getValue() : null;
    }

    /**
     * Clears all data from the queue
     * All elements are deleted. Elements in the algorithm queue are also deleted
     * @throws IllegalAccessError if file was not
     */
    @Override
    public void clear(){
        indexMap.clear();
        algo.clear();
        for (Path file: indexMap.values()) {
            fileStorage.deleteFile(file);
        }
    }

    /**
     * @return current size of the cache
     */
    @Override
    public int size() {
        return indexMap.size();
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return maxSize;
    }
}
