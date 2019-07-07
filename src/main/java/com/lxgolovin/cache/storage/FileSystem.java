package com.lxgolovin.cache.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FileSystem<K, V> implements Storage<K, V> {

    /**
     * If the directory is created temporary this prefix is used
     */
    private static final String TEMP_DIR_PREFIX = "fsStorage";

    /**
     *
     */
    private static final boolean EMPTY_STORAGE_DEFAULT = false;

    /**
     * Map to keep data
     */
    private final Map<K, Path> indexMap;

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    FileSystem() {
        this(null);
    }

    FileSystem(Path path) {
        this(path, FileSystem.EMPTY_STORAGE_DEFAULT);
    }

    FileSystem(Path path, boolean emptyStorage) {
        indexMap = new HashMap<>();

        if (path == null) {
            createTempDirectory();
        } else {
            createDirectory(path);
        }

        if (emptyStorage) {
            emptyDir();
        }
    }

    FileSystem(Path path, Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }

        indexMap = new HashMap<>();

        if (path == null) {
            createTempDirectory();
        } else {
            createDirectory(path);
        }

        emptyDir();
    }

    /**
     * Returns directory path where all files are kept
     *
     * @return path where data files kept
     */
    Path getDirectory() {
        return directory;
    }

    public V put(K key, V value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException();
        }

        Path filePath = null;
        V oldValue = null;

        if (containsKey(key)) {
            filePath = indexMap.get(key);
            oldValue = readFromFile(filePath);
        }

        if (needToStoreNewcomer(oldValue, value)) {
            writeToFile(key, value, filePath);
        }

        return oldValue;
    }

    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        // Need to move key as it was accessed. If false, return null
        if (!containsKey(key)) {
            return null;
        }

        Path path = indexMap.get(key);
        return readFromFile(path);
    }

    public boolean containsKey(K key) {
        return ((key != null) && indexMap.containsKey(key));
    }

    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }

        Path path = indexMap.remove(key);
        V value = readFromFile(path);
        deleteFile(path);

        return value;
    }

    public void clear(){
        indexMap.values().forEach(this::deleteFile);
        indexMap.clear();
    }

    public int size() {
        return indexMap.size();
    }
    public boolean isEmpty() { return indexMap.isEmpty(); }

    private void writeToFile(K key, V value, Path path) {
        Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);
        this.writeToFile(newcomer, path);
    }

    private void writeToFile(Map.Entry<K, V> entry, Path path) {
        Path filePath = (path == null) ? createFile() : path;

        try (OutputStream outputStream = Files.newOutputStream(filePath);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();
            indexMap.put(entry.getKey(), filePath);
        } catch (IOException e) {
            // TODO: sing a song
        }
    }

    private Path createFile() {
        try {
            return Files.createTempFile(directory, null, null);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * Gets entry from the specified path.
     *
     * @param path to the file with entry. Path cannot be null
     * @return entry stored in file by the path if present, else null.
     * @throws IllegalArgumentException if path is null.
     */
    @SuppressWarnings("unchecked")
    private V readFromFile(Path path) {
        if (path == null) {
            return null;
        }

        Map.Entry<K, V> entry;
        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            entry =  (Map.Entry<K, V>) objectInputStream.readObject();
            return (entry != null) ? entry.getValue() : null;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
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

    private boolean needToStoreNewcomer(V oldValue, V newValue) {
        return ((oldValue == null) || (!oldValue.equals(newValue)));
    }


    private void createTempDirectory() {
        try {
            directory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            directory.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    private void createDirectory(Path path) {
        File dir = new File(path.toString());

        // if path is present and not a directory
        if (dir.exists() & !dir.isDirectory()) {
            throw new IllegalAccessError();
        }

        // create the directory if doesn't exist
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                throw new IllegalAccessError();
            }
        }




        directory = dir.toPath();
    }

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