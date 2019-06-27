package com.lxgolovin.cache;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntryFileKeeperTest {

    private String directoryPath = "./TEMP/";
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

        assertThrows(IllegalArgumentException.class, () -> entryFileKeeper.writeToFile(entry, null));
        assertThrows(IllegalArgumentException.class, () -> entryFileKeeper.writeToFile(null, path));
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

    @Test
    void readAllFromDirectory() {
        Map<Integer, String> map = entryFileKeeper.readAllFromDirectory();
    }

    @Test
    void deleteFile() {
        Path path = entryFileKeeper.createTempFile();
        Map.Entry<Integer, String> entry = new AbstractMap.SimpleImmutableEntry<>(3, "String");
        assertTrue(entryFileKeeper.writeToFile(entry, path));

        assertTrue(entryFileKeeper.deleteFile(path));
        assertFalse(entryFileKeeper.deleteFile(path));
    }
}