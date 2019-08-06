package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.core.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link Storage} to keep data in files
 * @see Storage
 * @see MemoryStorage
 */
public class FileSystemStorage<K extends Serializable, V extends Serializable> implements Storage<K, V> {

    /**
     * If the directory is created temporary this prefix is used
     */
    private static final String TEMP_DIR_PREFIX = "fsStorage";

    /**
     * Sets the value for default cleaning of the storage directory
     * By default it is false
     */
    private static final boolean EMPTY_STORAGE_DEFAULT = false;

    private final Map<K, Path> indexMap;

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorage.class);

    public FileSystemStorage() {
        this(null);
    }

    public FileSystemStorage(Path path) {
        this(path, FileSystemStorage.EMPTY_STORAGE_DEFAULT);
    }

    public FileSystemStorage(Path path, boolean emptyStorage) {
        indexMap = new HashMap<>();
        createStorageDirectory(path);
        if (emptyStorage) {
            emptyDir();
        }
    }

    /**
     * @param map can be null. If it is, storage is created empty
     */
    public FileSystemStorage(Path path, Map<K, V> map) {
        indexMap = new HashMap<>();
        createStorageDirectory(path);
        emptyDir();

        if (map != null) {
            map.forEach(this::put);
        }
    }

    /**
     * If the storage is not empty, one can get all the data from it
     * as a map
     */
    public Map<K, V> getAll() {
        Map<K, V> loadedMap = new HashMap<>();
        try {
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .map(this::readEntryFromFile)
                    .filter(Optional::isPresent)
                    .forEach(e -> loadedMap.put(e.get().getKey(), e.get().getValue()));
            return loadedMap;
        } catch (IOException | SecurityException e) {
            throw new CacheException("Contact admin. Cannot read directory ".concat(directory.toString()));
        }
    }

    /**
     * @param key cannot be null
     * @param value cannot be null
     * @throws IllegalArgumentException if any key or value is null
     */
    public synchronized Optional<V> put(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        Path filePath = indexMap.get(key);
        Optional<V> oldValue = readValueFromFile(filePath);

        // element need to be updated
        if (!oldValue.isPresent() || !oldValue.get().equals(value)) {
            putDataToStorage(key, value, filePath);
        }

        return oldValue;
    }

    boolean putAll(Map<K, V> map) {
        boolean putAllSuccess = false;

        if (map != null) {
            map.forEach(this::put);
            putAllSuccess = true;
        }
        return putAllSuccess;
    }

    /**
     * @param key cannot be null
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        Path path = indexMap.get(key);
        return readValueFromFile(path);
    }

    public boolean containsKey(K key) {
        return ((key != null) && indexMap.containsKey(key));
    }

    /**
     * @param key cannot be null
     * @throws IllegalArgumentException if key is null
     */
    public synchronized Optional<V> remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        Path path = indexMap.get(key);
        Optional<V> removedValue = readValueFromFile(path);

        indexMap.remove(key);
        deleteFile(path);

        return removedValue;
    }

    public void clear(){
        indexMap.values().forEach(this::deleteFile);
        indexMap.clear();
    }

    public int size() {
        return indexMap.size();
    }

    public boolean isEmpty() { return indexMap.isEmpty(); }

    Path getDirectory() {
        return directory;
    }

    private void putDataToStorage(K key, V value, Path path) {
        try {
            Path filePath = (path == null) ? createFile() : path;

            Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);
            writeEntryToFile(newcomer, filePath);

            indexMap.put(key, filePath);
        } catch (IOException e) {
            logger.error("Unable to put data into storage: ", e);
        }
    }

    /**
     * Writes mapping key-value to file
     * @param entry mapping key-value
     * @param path path to the file. If null, file is created
     */
    private void writeEntryToFile(Map.Entry<K, V> entry, Path path) throws IOException {

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();
        }
    }

    /**
     * @return path to created temp file
     */
    private Path createFile() throws IOException {
        return Files.createTempFile(directory, null, null);
    }

    /**
     * @return entry stored in file by the path if present, else null.
     */
    private Optional<V> readValueFromFile(Path path) {
        return readEntryFromFile(path).map(Map.Entry::getValue);
    }

    /**
     * @param path to the file
     * @return optional entry from the file
     */
    @SuppressWarnings("unchecked")
    private Optional<Map.Entry<K, V>> readEntryFromFile(Path path) {
        if (path == null) {
            return Optional.empty();
        }

        Map.Entry<K, V> entry = null;

        try (InputStream inputStream = Files.newInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            entry = (Map.Entry<K, V>) objectInputStream.readObject();
            if (entry != null)
                indexMap.put(entry.getKey(), path);

        } catch (Exception e) {
            logger.error("Cannot read file {} from storage: {}", path.toUri(), e.getLocalizedMessage());
        }

        return Optional.ofNullable(entry);
    }

    /**
     * Deletes file by path
     * @param path to file
     */
    private void deleteFile(Path path) {
        if (path != null) {
            boolean fileDeleted = path.toFile().delete();
            if (fileDeleted) {
                logger.info("File {} deleted successfully", path.toUri());
            } else {
                logger.warn("File {} not deleted", path.toUri());
            }
        }
    }

    /**
     * Creates directory for the storage
     * @param path if empty, creates temporary directory
     */
    private void createStorageDirectory(Path path) {
        if (path == null) {
            createTempDirectory();
        } else {
            createDirectoryByPath(path);
        }
    }

    /**
     * creates temporary directory for storage
     */
    private void createTempDirectory() {
        try {
            directory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            directory.toFile().deleteOnExit();
        } catch (Exception e) {
            throw new CacheException("Cannot create temporary directory", e);
        }
    }

    /**
     * creates directory by specified path
     */
    private void createDirectoryByPath(Path path) {
        if (!path.toFile().isDirectory()) {
            try {
                directory = Files.createDirectory(path);
            } catch (Exception e) {
                throw new CacheException("Cannot create temporary directory", e);
            }
        } else {
            directory = path;
        }
    }

    /**
     * cleans storage directory
     */
    private void emptyDir() {
        try {
            Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .forEach(this::deleteFile);
        } catch (Exception e) {
            throw new CacheException("Cannot create temporary directory ".concat(directory.toString()), e);
        }
    }
}