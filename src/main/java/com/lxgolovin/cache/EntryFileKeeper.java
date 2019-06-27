package com.lxgolovin.cache;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This class creates directory and stores files inside. The files are serialized {@link java.util.Map.Entry}
 * Directory could be created as temporary or defined by user
 */
public class EntryFileKeeper<K extends Serializable, V extends Serializable> {

    /**
     * If the directory is created temporary this prefix is used
     */
    private final String TEMP_DIR_PREFIX = "fskeeper";

    /**
     * Defining the directory to keep all data
     */
    private Path directory;

    /**
     * Creates temporary directory to keep data
     */
    EntryFileKeeper() {
        this(null);
    }

    /**
     * Creates directory to keep data. If path parameter is null, the temporary one is used with
     * prefix, defined in {@link EntryFileKeeper#TEMP_DIR_PREFIX}
     *
     * @param path defined to keep data files
     */
    EntryFileKeeper(Path path) {
        if (path == null) {
            createTempDirectory();
        } else {
            createDirectory(path);
        }
    }

    /**
     * Returns directory path
     *
     * @return path where data files kept
     */
    Path getDirectory() {
        return directory;
    }

    /**
     * Creates temporary directory at initialisation phase
     */
    private void createTempDirectory() {
        try {
            directory = Files.createTempDirectory(TEMP_DIR_PREFIX);
            directory.toFile().deleteOnExit(); // TODO: strange behaviour, need to investigate
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }

    /**
     * Creates directory, defined at initialisation phase
     */
    private void createDirectory(Path pathName) {
        File dir = new File(pathName.toString());

        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalAccessError();
        }

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
     * Writes entry to the temporary file. If path is set, entry is written to the file.
     * If path is not set, a temporary file is created, using
     * {@link Files#createTempFile(String, String, FileAttribute[])}
     *
     * @param entry entry to be written to file
     * @param path for the temporary file. If null, file is created
     */
    boolean writeToFile(Map.Entry<K, V> entry, Path path) {
        if ((path == null) | (entry == null)) {
            throw new IllegalArgumentException();
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
     * @param path to the file with entry
     * @return entry stored in file by the path if present, else null
     * @throws IllegalAccessError if cannot access file by path or if class
     * of a serialized object cannot be found.
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
            throw new IllegalAccessError();
        }
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
    Map<K, V> readAllFromDirectory() {
        File dir = directory.toFile();

        if (dir.exists() && dir.isDirectory()) {
            File[] directoryListing = dir.listFiles();
            if (directoryListing == null) {
                return null;
            }

            Map<K, V> mapFromFiles = new HashMap<>();
            for (File f : dir.listFiles()) {
                if ( f!=null && f.isFile()) {
                    try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(f))) {
                        Map.Entry<K, V> entry = (Map.Entry<K, V>) objectInputStream.readObject();
                        mapFromFiles.put(entry.getKey(), entry.getValue());
                    } catch (IOException | ClassNotFoundException e) {
                        // File is empty or not readable. For the current case ignore it
                    }
                }
            }
            return mapFromFiles;
        } else {
            return null;
        }
    }

    /**
     * Creates file and returns path to the file
     *
     * @return path to newly created file
     * @throws IllegalAccessError if there was a error creating the file
     */
    Path createTempFile() {
        try {
            return Files.createTempFile(directory, null, null);
        } catch (IOException e) {
            throw new IllegalAccessError();
        }
    }
}
