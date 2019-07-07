package com.lxgolovin.cache.storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemTest {

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * create object to work with files
     */
    private final FileSystem<Integer, String> fileStorage = new FileSystem<>(Paths.get(directoryPath));

    /**
     * Check creating temporary directory
     */
    @Test
    void createTempDirectory() {
        FileSystem<Integer, Integer> fileStorage = new FileSystem<>();
        assertTrue(fileStorage.getDirectory().toFile().exists());
    }

    /**
     * Checking user defined directory is present
     */
    @Test
    void createUserDefinedDirectory() {
        assertTrue(fileStorage.getDirectory().toFile().exists());
        assertTrue(fileStorage.getDirectory().toFile().isDirectory());
        assertEquals(Paths.get(directoryPath), fileStorage.getDirectory());
    }

    /**
     * Test putting values into storage
     */
    @Test
    void putKeyValueToStorage() {
        assertNull(fileStorage.put(1,"One"));
        assertNull(fileStorage.put(2, "Two"));
        assertEquals("One", fileStorage.put(1, "Eleven"));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.put(3, null));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.put(null, "null"));
    }

    /**
     * Getting value from storage
     */
    @Test
    void getValueFromStorage() {
        assertNull(fileStorage.get(1));
        assertNull(fileStorage.put(1,"One"));
        assertEquals("One", fileStorage.get(1));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.get(null));
    }

    /**
     * checking if storage is empty
     */
    @Test
    void ifEmptyAndClean() {
        assertTrue(fileStorage.isEmpty());
        assertNull(fileStorage.put(1,"One"));
        assertFalse(fileStorage.isEmpty());
        fileStorage.clear();
        assertTrue(fileStorage.isEmpty());
    }

    /**
     * checking if storage contains key-value
     */
    @Test
    void ifContainsKey() {
        assertTrue(fileStorage.isEmpty());
        assertNull(fileStorage.put(1,"One"));
        assertTrue(fileStorage.containsKey(1));
        assertFalse(fileStorage.containsKey(null));
    }

    /**
     * Using constructor with option to delete all files at init phase
     */
    @Test
    void emptyStorageDirectoryOnInit() throws IOException {
        boolean emptyStorageDirectoryOnInit = true;
        FileSystem<Integer, String> emptyFileStorage = new FileSystem<>(Paths.get(directoryPath), emptyStorageDirectoryOnInit);
        assertEquals(Paths.get(directoryPath), emptyFileStorage.getDirectory());
        assertFalse(Files.walk(Paths.get(directoryPath))
                .anyMatch(Files::isRegularFile));
    }
}