package com.lxgolovin.cache.storage;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MemoryStorageTest {
    /**
     * create object to work with files
     */
    private final Storage<Integer, String> storage = new MemoryStorage<>();

    /**
     * Test putting values into storage
     */
    @Test
    void putKeyValueToStorage() {
        assertFalse(storage.put(1,"One").isPresent());
        assertFalse(storage.put(2, "Two").isPresent());
        assertEquals(Optional.of("One"), storage.put(1, "Eleven"));
        assertThrows(IllegalArgumentException.class, () -> storage.put(3, null));
        assertThrows(IllegalArgumentException.class, () -> storage.put(null, "null"));
    }

    /**
     * Getting value from storage
     */
    @Test
    void getValueFromStorage() {
        assertFalse(storage.get(1).isPresent());
        assertFalse(storage.put(1,"One").isPresent());
        assertEquals(Optional.of("One"), storage.get(1));
        assertThrows(IllegalArgumentException.class, () -> storage.get(null));
    }

    /**
     * checking if storage is empty
     */
    @Test
    void ifEmptyAndClean() {
        assertTrue(storage.isEmpty());
        assertFalse(storage.put(1,"One").isPresent());
        assertFalse(storage.isEmpty());
        storage.clear();
        assertTrue(storage.isEmpty());
    }

    /**
     * checking if storage contains key-value
     */
    @Test
    void ifContainsKey() {
        assertTrue(storage.isEmpty());
        assertFalse(storage.put(1,"One").isPresent());
        assertTrue(storage.containsKey(1));
        assertFalse(storage.containsKey(null));
    }

    @Test
    void putMapToStorage() {
        Map<Integer, Integer> map = new HashMap<>();
        IntStream.rangeClosed(1,10).forEach(x -> map.put(x, x*x));

        Storage<Integer, Integer> mapStorage = new MemoryStorage<>(map);

        assertEquals(10, mapStorage.size());
        assertFalse(mapStorage.isEmpty());
        assertEquals(Optional.of(100), mapStorage.get(10));
    }

    /**
     * Store data in files. Then create other storage and read all data from it
     */
    @Test
    void  getAllDataFromStorage() {
        // create empty storage
        Storage<Integer, Integer> emptyFileStorage = new MemoryStorage<>();
        IntStream.rangeClosed(1,10).forEach(x -> emptyFileStorage.put(x, x*x));

        Map<Integer, Integer> map = emptyFileStorage.getAll();
        assertEquals(emptyFileStorage.size(), map.size());

        assertEquals(100, map.get(10));
    }
}