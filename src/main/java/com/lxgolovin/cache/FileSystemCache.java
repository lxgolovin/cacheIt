package com.lxgolovin.cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.AbstractMap;
import java.util.HashMap;
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
public class FileSystemCache<K, V> implements Cache<K, V>  {
    // TODO: much code similar to MemoryCache code. Possibly need AbstractCache class to combine
    // TODO: move file handling methods to separate class

    /**
     * Prefix for the cache directories that are created temporary
     */
    private final String CACHE_TEMP_DIR_PREFIX = "fscache";

    /**
     * Map-index of files to cache data
     */
    private final Map<K, Path> indexMap;

    /**
     * Temporary directory for the cache files
     */
    private Path cacheDir;

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
     * Creates file system cache with defined algorithm. Key-values are got from the directory.
     * If the directory is empty, creates an empty cache with default size {@link Cache#DEFAULT_CACHE_SIZE}.
     * If the directory is not empty, cache is created with maxSize equal to number of cache files in the directory.
     *
     * @param algorithm specifies algorithm type that is used by the cache
     * @param path to the directory to check files
     */
    public FileSystemCache(CacheAlgorithm<K> algorithm, Path path) {
        if (path.toFile().exists() && path.toFile().isDirectory()) {
            cacheDir = path;
        } else {
            createTempDirectory();
        }

        algo = algorithm;
        maxSize = DEFAULT_CACHE_SIZE;
        // check the files in the directory
        // read all files from the directory and get data
        // put data to the map
        indexMap = new HashMap<>();
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

        createTempDirectory();

        indexMap = new HashMap<>();
        putAll(map);
    }

    /**
     * Creates temporary directory at initialisation phase
     */
    private void createTempDirectory() {
        try {
            cacheDir = Files.createTempDirectory(CACHE_TEMP_DIR_PREFIX);
            cacheDir.toFile().deleteOnExit(); // TODO: strange behaviour, need to investigate
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
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

        Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);

        Map.Entry<K, V> poppedEntry = null;
        if ((size() == maxSize) && (!contains(key))) {
            // using deletion by algorithm
            poppedEntry = pop();
        }

        Map.Entry<K, V> replacedEntry = null;
        Path filePath = null;
        if (algo.shift(key)) {
            // need to get file, read old value
            filePath = indexMap.get(key);
            replacedEntry = readFromFile(filePath);
        }
        writeToFile(newcomer, filePath);

        return (poppedEntry == null) ? replacedEntry : poppedEntry;
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
        Map.Entry<K, V> entry = readFromFile(path);

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
        Map.Entry<K, V> entry = readFromFile(path);
        deleteFile(path);

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
            deleteFile(file);
        }
    }

    /**
     * Deletes file by path
     * @param path to file
     * @throws IllegalAccessError if the file was not deleted as not accessible
     */
    private void deleteFile(Path path) {
        if (!path.toFile().delete()) {
            throw new IllegalAccessError();
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

    /**
     * Gets entry from the specified path.
     *
     * @param path to the file with entry
     * @return entry stored in file by the path if present, else null
     * @throws IllegalAccessError if cannot access file by path or if class
     * of a serialized object cannot be found.
     */
    @SuppressWarnings("unchecked")
    private Map.Entry<K, V> readFromFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return  (Map.Entry<K, V>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * Writes entry to the temporary file. If path is set, entry is written to the file.
     * If path is not set, a temporary file is created, using
     * {@link Files#createTempFile(String, String, FileAttribute[])}
     *
     * @param entry entry to be written to file
     * @param path for the temporary file. If null, file is created
     * @throws IllegalAccessError if the path is not accessible and entry was not written to file
     */
    private void writeToFile(Map.Entry<K, V> entry, Path path) {
        if (path == null) {
            path = createTempFile();
        }

        try ( OutputStream outputStream = Files.newOutputStream(path);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();

            indexMap.put(entry.getKey(), path);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * Creates file and returns path to the file
     *
     * @return path to newly created file
     * @throws IllegalAccessError if there was a error creating the file
     */
    private Path createTempFile() {
        try {
            return Files.createTempFile(cacheDir, null, null);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }
}
