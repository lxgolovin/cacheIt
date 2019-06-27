package com.lxgolovin.file;


import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

/**
 * This class creates directory and stores files inside. The files are serialized {@link java.util.Map.Entry<K, V>>}
 * Directory could be created as temporary or defined by user
 */
public class EntryFileKeeper<K, V> {

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
}
