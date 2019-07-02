package com.lxgolovin.cache.type;

import com.lxgolovin.cache.type.FileStorage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class creates tests for the {@link FileStorage}
 * The idea is to create a directory and store files inside. The files are serialized {@link java.util.Map.Entry}
 * Directory could be created as temporary or defined by user.
 *
 * If not directory is specified, a temporary one is used.
 *
 * Class gives a possibility to store data in files and get data back.
 */
class FileStorageTest {

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * create object to work with files
     */
    private final FileStorage<Integer, String> fileStorage = new FileStorage<>(Paths.get(directoryPath));

    /**
     * Check creating temporary directory
     */
    @Test
    void createTempDirectory() {
        FileStorage<Integer, Integer> fileStorageTemp = new FileStorage<>();
        assertTrue(fileStorageTemp.getDirectory().toFile().exists());
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
     * Check creating file to keep data
     */
    @Test
    void createTempFile() {
        Path path = fileStorage.createFile();

        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isFile());
    }

    /**
     * Test writing to file
     */
    @Test
    void writeEntryToFile() {
        Path path = fileStorage.createFile();
        assertTrue(fileStorage.writeToFile(1, "String", path));

        assertFalse(fileStorage.writeToFile(1, "String", null));
        assertFalse(fileStorage.writeToFile(null, path));
    }

    /**
     * Testing reading from file
     */
    @Test
    void readEntryFromFile() {
        Path path = fileStorage.createFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(2, "String");
        assertTrue(fileStorage.writeToFile(entry, path));

        assertEquals(entry, fileStorage.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.readFromFile(null));
    }

    /**
     * This test is created to get the situation when data stored in files got different types
     */
    @Test
    void readWrongEntryFromFile() {
        FileStorage<String, String> fileStorageWrong = new FileStorage<>(Paths.get(directoryPath));

        Path path = fileStorageWrong.createFile();
        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("String", "String");
        assertTrue(fileStorageWrong.writeToFile(entry, path));

        assertEquals(entry, fileStorageWrong.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> fileStorageWrong.readFromFile(null));
    }

    /**
     * reading data from all files.
     * TODO: need to check the issue with different types
     */
     @Test
     void readAllFromDirectory() {
        List<FileStorage<Integer, String>.OutputNode<Path>> list =
                fileStorage.readAllFromDirectory();
        assertFalse(list.isEmpty());
     }

    /**
     * testing deleting files
     */
    @Test
    void deleteFile() {
        Path path = fileStorage.createFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(3, "String");
        assertTrue(fileStorage.writeToFile(entry, path));

        assertTrue(fileStorage.deleteFile(path));
        assertFalse(fileStorage.deleteFile(path));
        assertFalse(path.toFile().exists());
    }

    /**
     * tests the possibility to clear directory at initialization phase
     */
    @Test
    void WouldEmptyDirectory() {
        FileStorage<Integer, String> fileStorageDeleteDir =
                new FileStorage<>(Paths.get(directoryPath), true);
        assertTrue(fileStorageDeleteDir.readAllFromDirectory().isEmpty());
    }
}