package com.lxgolovin.cache.storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemStorageTest {

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * create object to work with files
     */
    private final FileSystemStorage<Integer, String> fileStorage = new FileSystemStorage<>(Paths.get(directoryPath));

    /**
     * Check creating temporary directory
     */
    @Test
    void createTempDirectory() {
        FileSystemStorage<Integer, Integer> tempStorage = new FileSystemStorage<>();
        assertTrue(tempStorage.getDirectory().toFile().exists());
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
        assertFalse(fileStorage.put(1,"One").isPresent());
        assertFalse(fileStorage.put(2, "Two").isPresent());
        assertEquals(Optional.of("One"), fileStorage.put(1, "Eleven"));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.put(3, null));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.put(null, "null"));
    }

    /**
     * Getting value from storage
     */
    @Test
    void getValueFromStorage() {
        assertFalse(fileStorage.get(1).isPresent());
        assertFalse(fileStorage.put(1,"One").isPresent());
        assertEquals(Optional.of("One"), fileStorage.get(1));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.get(null));
    }

    /**
     * checking if storage is empty
     */
    @Test
    void ifEmptyAndClean() {
        assertTrue(fileStorage.isEmpty());
        assertFalse(fileStorage.put(1,"One").isPresent());
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
        assertFalse(fileStorage.put(1,"One").isPresent());
        assertTrue(fileStorage.containsKey(1));
        assertFalse(fileStorage.containsKey(null));
    }

    /**
     * Using constructor with option to delete all files at init phase
     */
    @Test
    void emptyStorageDirectoryOnInit() throws IOException {
        FileSystemStorage<Integer, String> emptyFileStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        assertEquals(Paths.get(directoryPath), emptyFileStorage.getDirectory());
        try (Stream<Path> dirTree =Files.walk(Paths.get(directoryPath))) {
            assertFalse(dirTree.anyMatch(Files::isRegularFile));
        }
    }

    @Test
    void putMapToStorageInConstructor() {
        Map<Integer, Integer> map = new HashMap<>();
        IntStream.rangeClosed(1,10).forEach(x -> map.put(x, x*x));

        FileSystemStorage<Integer, Integer> mapFileStorage = new FileSystemStorage<>(Paths.get(directoryPath), map);

        assertEquals(10, mapFileStorage.size());
        assertFalse(mapFileStorage.isEmpty());
        assertEquals(Optional.of(100), mapFileStorage.get(10));
    }

    @Test
    void putMapToStorageByPutAll() {
        Map<Integer, Integer> map = new HashMap<>();
        IntStream.rangeClosed(1,10).forEach(x -> map.put(x, x*x));

        FileSystemStorage<Integer, Integer> mapFileStorage = new FileSystemStorage<>();
        assertTrue(mapFileStorage.putAll(map));

        assertEquals(10, mapFileStorage.size());
        assertFalse(mapFileStorage.isEmpty());
        assertEquals(Optional.of(100), mapFileStorage.get(10));
    }

    /**
     * Store data in files. Then create other storage and read all data from it
     */
    @Test
    void  getAllDataFromStorage() {
        // create empty storage
        FileSystemStorage<Integer, Integer> emptyFileStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        IntStream.rangeClosed(1,10).forEach(x -> emptyFileStorage.put(x, x*x));

        FileSystemStorage<Integer, Integer> storage = new FileSystemStorage<>(Paths.get(directoryPath));
        Map<Integer, Integer> map = storage.getAll();
        assertEquals(emptyFileStorage.size(), storage.size());
        assertEquals(map.size(), storage.size());

        assertEquals(100, map.get(10));
    }
}