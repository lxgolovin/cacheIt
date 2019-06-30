package com.lxgolovin.cache.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class creates tests for the {@link Storage}
 * The idea is to create a directory and store files inside. The files are serialized {@link java.util.Map.Entry}
 * Directory could be created as temporary or defined by user.
 *
 * If not directory is specified, a temporary one is used.
 *
 * Class gives a possibility to store data in files and get data back.
 */
class StorageTest {

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * create object to work with files
     */
    private final Storage<Integer, String> storage = new Storage<>(Paths.get(directoryPath));

    /**
     * Check creating temporary directory
     */
    @Test
    void createTempDirectory() {
        Storage<Integer, Integer> storageTemp = new Storage<>();
        assertTrue(storageTemp.getDirectory().toFile().exists());
    }

    /**
     * Checking user defined directory is present
     */
    @Test
    void createUserDefinedDirectory() {
        assertTrue(storage.getDirectory().toFile().exists());
        assertTrue(storage.getDirectory().toFile().isDirectory());
        assertEquals(Paths.get(directoryPath), storage.getDirectory());
    }

    /**
     * Check creating file to keep data
     */
    @Test
    void createTempFile() {
        Path path = storage.createFile();

        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isFile());
    }

    /**
     * Test writing to file
     */
    @Test
    void writeEntryToFile() {
        Path path = storage.createFile();
        assertTrue(storage.writeToFile(1, "String", path));

        assertFalse(storage.writeToFile(1, "String", null));
        assertFalse(storage.writeToFile(null, path));
    }

    /**
     * Testing reading from file
     */
    @Test
    void readEntryFromFile() {
        Path path = storage.createFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(2, "String");
        assertTrue(storage.writeToFile(entry, path));

        assertEquals(entry, storage.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> storage.readFromFile(null));
    }

    /**
     * This test is created to get the situation when data stored in files got different types
     */
    @Test
    void readWrongEntryFromFile() {
        Storage<String, String> storageWrong = new Storage<>(Paths.get(directoryPath));

        Path path = storageWrong.createFile();
        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("String", "String");
        assertTrue(storageWrong.writeToFile(entry, path));

        assertEquals(entry, storageWrong.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> storageWrong.readFromFile(null));
    }

    /**
     * reading data from all files.
     * TODO: need to check the issue with different types
     */
     @Test
     void readAllFromDirectory() {
        List<Storage<Integer, String>.OutputNode<Path>> list =
                storage.readAllFromDirectory();
        assertFalse(list.isEmpty());
     }

    /**
     * testing deleting files
     */
    @Test
    void deleteFile() {
        Path path = storage.createFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(3, "String");
        assertTrue(storage.writeToFile(entry, path));

        assertTrue(storage.deleteFile(path));
        assertFalse(storage.deleteFile(path));
        assertFalse(path.toFile().exists());
    }

    /**
     * tests the possibility to clear directory at initialization phase
     */
    @Test
    void WouldEmptyDirectory() {
        Storage<Integer, String> storageDeleteDir =
                new Storage<>(Paths.get(directoryPath), true);
        assertTrue(storageDeleteDir.readAllFromDirectory().isEmpty());
    }
}