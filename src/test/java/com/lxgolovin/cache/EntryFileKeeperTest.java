package com.lxgolovin.cache;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class creates tests for the {@link EntryFileKeeper}
 * The idea is to create a directory and store files inside. The files are serialized {@link java.util.Map.Entry}
 * Directory could be created as temporary or defined by user.
 *
 * If not directory is specified, a temporary one is used.
 *
 * Class gives a possibility to store data in files and get data back.
 */
class EntryFileKeeperTest {

    /**
     * Directory for testing
     */
    private final String directoryPath = "./TEMP/";

    /**
     * create object to work with files
     */
    private final EntryFileKeeper<Integer, String> entryFileKeeper = new EntryFileKeeper<>(Paths.get(directoryPath));

    /**
     * Check creating temporary directory
     */
    @Test
    void createTempDirectory() {
        EntryFileKeeper<Integer, Integer> entryFileKeeperTemp = new EntryFileKeeper<>();
        assertTrue(entryFileKeeperTemp.getDirectory().toFile().exists());
    }

    /**
     * Checking user defined directory is present
     */
    @Test
    void createUserDefinedDirectory() {
        assertTrue(entryFileKeeper.getDirectory().toFile().exists());
        assertTrue(entryFileKeeper.getDirectory().toFile().isDirectory());
        assertEquals(Paths.get(directoryPath), entryFileKeeper.getDirectory());
    }

    /**
     * Check creating file to keep data
     */
    @Test
    void createTempFile() {
        Path path = entryFileKeeper.createTempFile();

        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isFile());
    }

    /**
     * Test writing to file
     */
    @Test
    void writeEntryToFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(1, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertFalse(entryFileKeeper.writeToFile(entry, null));
        assertFalse(entryFileKeeper.writeToFile(null, path));
    }

    /**
     * Testing reading from file
     */
    @Test
    void readEntryFromFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(2, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertEquals(entry, entryFileKeeper.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> entryFileKeeper.readFromFile(null));
    }

    /**
     * This test is created to get the situation when data stored in files got different types
     */
    @Test
    void readWrongEntryFromFile() {
        EntryFileKeeper<String, String> entryFileKeeperWrong = new EntryFileKeeper<>(Paths.get(directoryPath));

        Path path = entryFileKeeperWrong.createTempFile();
        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("String", "String");
        assertTrue(entryFileKeeperWrong.writeToFile(entry, path));

        assertEquals(entry, entryFileKeeperWrong.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> entryFileKeeperWrong.readFromFile(null));
    }

    /**
     * reading data from all files.
     * TODO: need to check the issue with different types
     */
     @Test
     void readAllFromDirectory() {
        List<EntryFileKeeper<Integer, String>.OutputNode<Path>> list =
                entryFileKeeper.readAllFromDirectory();
        assertFalse(list.isEmpty());
     }

    /**
     * testing deleting files
     */
    @Test
    void deleteFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(3, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertTrue(entryFileKeeper.deleteFile(path));
        assertFalse(entryFileKeeper.deleteFile(path));
        assertFalse(path.toFile().exists());
    }

    /**
     * tests the possibility to clear directory at initialization phase
     */
    @Test
    void WouldEmptyDirectory() {
        EntryFileKeeper<Integer, String> entryFileKeeperDeleteDir =
                new EntryFileKeeper<>(Paths.get(directoryPath), true);
        assertTrue(entryFileKeeperDeleteDir.readAllFromDirectory().isEmpty());
    }
}