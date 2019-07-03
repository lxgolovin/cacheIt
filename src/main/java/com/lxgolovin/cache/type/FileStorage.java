package com.lxgolovin.cache.type;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class creates directory and stores files inside.
 * Directory could be created as temporary or defined by user.
 *
 * If not directory is specified, a temporary one is used. The prefix for the temporary directory is
 * {@link FileStorage#TEMP_DIR_PREFIX}
 *
 * Class gives a possibility to store data in files and get data back.
 */
final class FileStorage<K, V> {

    /**
     * If the directory is created temporary this prefix is used
     */
    private final String TEMP_DIR_PREFIX = "fsStorage";

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    /**
     * Empty constructor creates temporary directory to keep data
     */
    FileStorage() {
        this(null);
    }

    /**
     * Creates directory to keep data. If path parameter is null, the temporary one is used with
     * prefix, defined in {@link FileStorage#TEMP_DIR_PREFIX}
     *
     * @param path defined to keep data files
     */
    FileStorage(Path path) {
        this(path, false);
    }

    /**
     * Creates directory to keep data. If path parameter is null, the temporary one is used with
     * prefix, defined in {@link FileStorage#TEMP_DIR_PREFIX}
     * Additional flag added to clean directory at initialization phase
     *
     * @param path defined to keep data files
     * @param cleanDirectory true if need to delete the directory if exists, else false
     */
    FileStorage(Path path, boolean cleanDirectory) {
        if (path == null) {
            createTempDirectory();
        } else {
            createDirectory(path, cleanDirectory);
        }
    }

    /**
     * Returns directory path where all files are kept
     *
     * @return path where data files kept
     */
    Path getDirectory() {
        return directory;
    }

    /**
     * Creates temporary directory at initialisation phase
     *
     * @throws IllegalAccessError if cannot create temporary directory
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
     * Creates directory by path and stores the value in {@link FileStorage#directory}, which could be
     * got by {@link FileStorage#getDirectory()}
     *
     * @param path of the directory to be created
     * @param deleteFilesInDirectory true if need to delete the directory if exists, else false
     * @throws IllegalAccessError if the path is a file, but not a directory and
     *          if the the directory could not be created.
     */
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

    /**
     * Deletes file by path
     * @param path to file
     */
    boolean deleteFile(Path path) {
        return path.toFile().delete();
    }

    /**
     * Writes entry to the file by path. Returns false if not success, else true.
     *
     * @param key for the value to be stored
     * @param value mapping for the key to be stored
     * @param path for the temporary file. Cannot be null
     * @return true if written to file, else false
     */
    boolean writeToFile(K key, V value, Path path) {
        if ((key == null) | (value == null)) {
            return false;
        }

        Map.Entry<K, V> newcomer = new AbstractMap.SimpleImmutableEntry<>(key, value);
        return  this.writeToFile(newcomer, path);
    }

    /**
     * Writes entry to the file by path. Returns false if not success, else true.
     *
     * @param entry entry to be written to file, cannot be null
     * @param path for the temporary file. Cannot be null
     */
    boolean writeToFile(Map.Entry<K, V> entry, Path path) {
        if ((path == null) | (entry == null)) {
            return false;
        }

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(entry);
            objectOutputStream.flush();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets entry from the specified path.
     *
     * @param path to the file with entry. Path cannot be null
     * @return entry stored in file by the path if present, else null.
     * @throws IllegalArgumentException if path is null.
     */
    @SuppressWarnings("unchecked")
    Map.Entry<K, V> readFromFile(Path path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }

        try (InputStream inputStream = Files.newInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (Map.Entry<K, V>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Reads all files in the directory and tries to deserialize objects. The result is a map with key-values
     *
     * @return map with all deserialized objects. If the directory is empty or not present, the return would be null
     */
    List<OutputNode<Path>> readAllFromDirectory() {
        File dir = directory.toFile();

        if (dir.exists() && dir.isDirectory()) {

            File[] directoryListing = dir.listFiles();
            if (directoryListing == null) {
                return null;
            }

            List<OutputNode<Path>> dataReadFromFiles = new LinkedList<>();
            for (File f : directoryListing) {
                if ((f != null) && f.isFile()) {

                    Map.Entry<K, V> entry = this.readFromFile(f.toPath());
                    if (entry != null) {
                        OutputNode<Path> node = new OutputNode<>(entry.getKey(), entry.getValue(), f.toPath());
                        dataReadFromFiles.add(node);
                    }
                }
            }
            
            return dataReadFromFiles;
        }

        return null;
    }

    /**
     * Creates file and returns path to the file. The file is created in the directory {@link FileStorage#directory}
     *
     * @return path to newly created file
     * @throws IllegalAccessError if there was a error creating the file
     */
    Path createFile() {
        try {
            return Files.createTempFile(directory, null, null);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * Inner class to create a node for the output during reading all data files
     * in one directory. During this step you need 3 parameters: key-value-path of the file.
     * As the class is only a helping class, all parameters are immutable
     *
     * @param <P> path of the file, were data is stored
     */
    final class OutputNode<P> {
        private final K key;
        private final V value;
        private final P path;

        OutputNode(K key, V value, P path){
            this.key = key;
            this.value = value;
            this.path = path;
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        K getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry
         */
        V getValue() {
            return value;
        }

        /**
         * Returns the value of path corresponding to this entry.
         *
         * @return the value of path corresponding to this entry
         */
        P getPath() {
            return path;
        }
    }
}