package com.lxgolovin.cache.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Map-index to keep paths to the files of the storage
     */
    private final Map<K, Path> indexMap;

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    /**
     * creates storage with temporary directory
     */
    public FileSystemStorage() {
        this(null);
    }

    /**
     *
     * @param path to keep files of the storage
     */
    public FileSystemStorage(Path path) {
        this(path, FileSystemStorage.EMPTY_STORAGE_DEFAULT);
    }

    public FileSystemStorage(Path path, boolean emptyStorage) {
        indexMap = new HashMap<>();
        createStorageDirectory(path);
        if (emptyStorage) {
            emptyDir();
            // TODO: need to check this with Mike
//        } else {
//            getAll();
        }
    }

    /**
     * Creates storage by map. Uf the map is null, creates empty storage
     * @param path to the storage
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
     * @return map of key-values, stored in storage
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
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * @return path where data files kept
     */
    Path getDirectory() {
        return directory;
    }

    /**
     * @param key cannot be null
     * @param value cannot be null
     * @throws IllegalArgumentException if any key or value is null
     */
    public Optional<V> put(K key, V value) {
        // TODO: create putAll to load map to storage
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        Path filePath = indexMap.get(key);
        Optional<V> oldValue = readValueFromFile(filePath);

        // element need to be updated
        if (!oldValue.isPresent() || !oldValue.get().equals(value)) {
            writeToFile(key, value, filePath);
        }

        return oldValue;
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
    public Optional<V> remove(K key) {
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

    /**
     * Writes mapping key-value to file
     */
    private void writeToFile(K key, V value, Path path) {
        Path filePath = (path == null) ? createFile() : path;

        Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);
        this.writeEntryToFile(newcomer, filePath);
        indexMap.put(key, filePath); // TODO: need to split in 2 methods
    }

    /**
     * Writes mapping key-value to file
     * @param entry mapping key-value
     * @param path path to the file. If null, file is created
     */
    private void writeEntryToFile(Map.Entry<K, V> entry, Path path) {

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();
        } catch (IOException e) {
            // TODO: sing a song. To work with exception
        }
    }

    /**
     * @return path to created temp file
     */
    private Path createFile() {
        try {
            return Files.createTempFile(directory, null, null);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
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
        Map.Entry<K, V> entry = null;

        try (InputStream inputStream = Files.newInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            entry = (Map.Entry<K, V>) objectInputStream.readObject();
            if (entry != null)
                indexMap.put(entry.getKey(), path);

        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            // TODO: to implement my exception
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
            System.out.println(fileDeleted); // TODO: create a logger
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
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * creates directory by specified path
     */
    private void createDirectoryByPath(Path path) {
        if (!path.toFile().isDirectory()) {
            try {
                directory = Files.createDirectory(path);
            } catch (IOException e) {
                // TODO: to sing a song
                throw new IllegalAccessError();
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
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }
}