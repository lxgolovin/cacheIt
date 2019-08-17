package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.core.CacheException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of {@link Storage} to keep data in files
 *
 * @see Storage
 * @see MemoryStorage
 */
@ThreadSafe
@Immutable
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

    @GuardedBy("this")
    private final Map<K, Path> indexMap;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Defining the directory to keep all data
     */
    private final Path directory;

    private final Logger logger = LoggerFactory.getLogger(FileSystemStorage.class);

    public FileSystemStorage() {
        this(null);
    }

    public FileSystemStorage(Path path) {
        this(path, FileSystemStorage.EMPTY_STORAGE_DEFAULT);
    }

    public FileSystemStorage(Path path, boolean emptyStorage) {
        this.directory = (path != null && checkDirectoryByPath(path))
                ? path
                : createTempDirectory();
        if (emptyStorage) {
            emptyDir();
        }
        indexMap = new HashMap<>();
    }

    /**
     * @param map can be null. If it is, storage is created empty
     */
    public FileSystemStorage(Path path, Map<K, V> map) {
        indexMap = new HashMap<>();
        this.directory = (path != null && checkDirectoryByPath(path))
                ? path
                : createTempDirectory();
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
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param key   cannot be null
     * @param value cannot be null
     * @throws IllegalArgumentException if any key or value is null
     */
    public Optional<V> put(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        lock.writeLock().lock();
        try {
            Optional<V> oldValue = get(key);
            // element need to be updated
            if (!oldValue.isPresent() || !oldValue.get().equals(value)) {
                putDataToStorage(key, value, indexMap.get(key));
            }

            return oldValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ! rewrite
    boolean putAll(Map<K, V> map) {
        if (map == null)
            return false;

        lock.writeLock().lock();
        try {
            map.forEach(this::put);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param key cannot be null
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        lock.readLock().lock();
        try {
            Path path = indexMap.get(key);
            return readValueFromFile(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(K key) {
        lock.readLock().lock();
        try {
            return ((key != null) && indexMap.containsKey(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param key cannot be null
     * @throws IllegalArgumentException if key is null
     */
    public Optional<V> remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        lock.writeLock().lock();
        try {
            Optional<V> removedValue = get(key);

            Path path = indexMap.remove(key);
            deleteFile(path);

            return removedValue;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            indexMap.values().forEach(this::deleteFile);
            indexMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return indexMap.size();

        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return indexMap.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

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
     *
     * @param entry mapping key-value
     * @param path  path to the file. If null, file is created
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

        } catch (ClassNotFoundException | InvalidClassException | OptionalDataException e) {
            logger.error("Cannot get data from file {} from storage: {}", path.toUri(), e.getLocalizedMessage());
        } catch (SecurityException e) {
            throw new CacheException("No rights to read the storage directory. Check access rights", e);
        } catch (IOException e) {
            throw new CacheException("IO error. Please check your storage drive", e);
        }
        return Optional.ofNullable(entry);
    }

    /**
     * Deletes file by path
     *
     * @param path to file
     */
    private void deleteFile(Path path) {
        if (path != null) {
            boolean fileDeleted = path.toFile().delete();
            if (!fileDeleted) {
                logger.warn("File {} not deleted", path.toUri());
            }
        }
    }

    private Path createTempDirectory() {
        try {
            Path path = Files.createTempDirectory(TEMP_DIR_PREFIX);
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception e) {
            throw new CacheException("Cannot create temporary directory", e);
        }
    }

    private boolean checkDirectoryByPath(Path path) {
        if (path.toFile().isDirectory()) {
            return true;
        }

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

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