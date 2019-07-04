package com.lxgolovin.cache.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileSystem<K, V> implements Storage<K, V> {

    /**
     * Map to keep data
     */
    private final Map<K, Path> indexMap;

    /**
     * If the directory is created temporary this prefix is used
     */
    private final String TEMP_DIR_PREFIX = "fsStorage";

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    public FileSystem() {
        indexMap = new HashMap<>();
    }

    public FileSystem(Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException();
        }

        indexMap = new HashMap<>();
        createDirectory(Paths.get("./TEMP"), true);
        map.forEach(this::put);
//            cacheMap = map;
    }

    public V put(K key, V value) {

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
        // Need to move key as it was accessed. If false, return null
        if (!containsKey(key)) {
            return null;
        }

        Path path = indexMap.get(key);
        V value = readFromFile(path);

        return value;
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




    private boolean writeToFile(K key, V value, Path path) {
        if ((key == null) | (value == null)) {
            return false;
        }

        Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);
        return  this.writeToFile(newcomer, path);
    }


    private boolean writeToFile(Map.Entry<K, V> entry, Path path) {
        if (entry == null) {
            return false;
        }

        Path filePath = (path == null) ? createFile() : path;

        try (OutputStream outputStream = Files.newOutputStream(filePath);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();
        } catch (IOException e) {
            return false;
        }

        indexMap.put(entry.getKey(), filePath);
        return true;
    }

    Path createFile() {
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
    private boolean deleteFile(Path path) {
        return ((path != null) && path.toFile().delete());
    }

    private boolean needToStoreNewcomer(V oldValue, V newValue) {
        return ((oldValue == null) || (!oldValue.equals(newValue)));
//        return ((oldValue != null) && (!oldValue.equals(newValue)));
    }


    private void createTempDirectory() {
        try {
            directory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            directory.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    private void createDirectory(Path path, boolean deleteFilesInDirectory) {
        File dir = new File(path.toString());

        // if path is present and not a directory
        if (dir.exists() & !dir.isDirectory()) {
            throw new IllegalAccessError();
        }

        if (dir.exists() & dir.isDirectory() && deleteFilesInDirectory) {
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                Arrays.stream(directoryListing)
                        .filter(File::isFile)
                        .forEach(File::delete);
            }
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
}