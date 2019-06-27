package com.lxgolovin.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class EntryFileKeeperTest {

    @Test
    void createTempDirectory() {
        EntryFileKeeper entryFileKeeper = new EntryFileKeeper();
        assertTrue(entryFileKeeper.getDirectory().toFile().exists());
    }

    @Test
    void createUserDefinedDirectory() {
        String pathString = "./TEMP/";
        EntryFileKeeper entryFileKeeper = new EntryFileKeeper(Paths.get(pathString));
        assertTrue(entryFileKeeper.getDirectory().toFile().exists());
        assertTrue(entryFileKeeper.getDirectory().toFile().isDirectory());
        assertEquals(Paths.get(pathString), entryFileKeeper.getDirectory());
    }
}