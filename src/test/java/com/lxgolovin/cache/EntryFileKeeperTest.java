package com.lxgolovin.cache;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

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
    private String directoryPath = "./TEMP/";

    /**
     *
     */
    private EntryFileKeeper<Integer, String> entryFileKeeper = new EntryFileKeeper<>(Paths.get(directoryPath));

    @Test
    void createTempDirectory() {
        EntryFileKeeper<Integer, Integer> entryFileKeeperTemp = new EntryFileKeeper<>();
        assertTrue(entryFileKeeperTemp.getDirectory().toFile().exists());
    }

    @Test
    void createUserDefinedDirectory() {
        assertTrue(entryFileKeeper.getDirectory().toFile().exists());
        assertTrue(entryFileKeeper.getDirectory().toFile().isDirectory());
        assertEquals(Paths.get(directoryPath), entryFileKeeper.getDirectory());
    }

    @Test
    void createTempFile() {
        Path path = entryFileKeeper.createTempFile();

        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isFile());
    }

    @Test
    void writeEntryToFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(1, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertFalse(entryFileKeeper.writeToFile(entry, null));
        assertFalse(entryFileKeeper.writeToFile(null, path));
    }

    @Test
    void readEntryFromFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(2, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertEquals(entry, entryFileKeeper.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> entryFileKeeper.readFromFile(null));
    }

    @Test
    void readWrongEntryFromFile() {
        EntryFileKeeper<String, String> entryFileKeeperWrong = new EntryFileKeeper<>(Paths.get(directoryPath));

        Path path = entryFileKeeperWrong.createTempFile();
        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("String", "String");
        assertTrue(entryFileKeeperWrong.writeToFile(entry, path));

        assertEquals(entry, entryFileKeeperWrong.readFromFile(path));
        assertThrows(IllegalArgumentException.class, () -> entryFileKeeperWrong.readFromFile(null));
    }

    // TODO: need to implement
     @Test
     void readAllFromDirectory() {
        Map<Integer, String> map = entryFileKeeper.readAllFromDirectory();
        assertEquals("String", map.get(1));
     }

    @Test
    void deleteFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(3, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertTrue(entryFileKeeper.deleteFile(path));
        assertFalse(entryFileKeeper.deleteFile(path));
    }

    @Test
    void WouldEmptyDirectory() {
        Map<Integer, String> map = new HashMap<>();
        EntryFileKeeper<Integer, String> entryFileKeeperDeleteDir = new EntryFileKeeper<>(Paths.get(directoryPath), true);
        assertEquals(map, entryFileKeeperDeleteDir.readAllFromDirectory());
    }
}