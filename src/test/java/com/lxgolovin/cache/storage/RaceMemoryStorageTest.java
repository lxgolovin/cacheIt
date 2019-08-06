package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RaceMemoryStorageTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private static final Map<Integer, String> map = new HashMap<>();

    private final Storage<Integer, String> storage = new MemoryStorage<>();

    @BeforeAll
    static void setUp() {
        for (int i = 0; i < threadsTotal; i++) {
            map.put(i, String.valueOf(Math.random() * 100));
        }
    }

    @Test
    void putKeyToHashMapSleep() throws InterruptedException {
        map.forEach((k, v) ->
                exec.execute(() ->
                        storage.put(k, v)));

        TimeUnit.SECONDS.sleep(3); // wait all finished
        assertEquals(threadsTotal, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @Test
    void putKeyToHashMapFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        map.forEach((k, v) ->
                futures.add(CompletableFuture.runAsync(() ->
                        storage.put(k, v), exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(threadsTotal, storage.size());
        map.forEach((k, v) -> {
            assertTrue(storage.containsKey(k));
            assertTrue(storage.get(k).isPresent());
            assertEquals(Optional.of(v), storage.get(k));
        });
    }

    @Test
    void putKeyToHashMapLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        map.forEach((k, v) -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                latch.countDown();
                latch.await();
                storage.put(k, v);
            } catch (InterruptedException e) {
                // just skip it and finish
            }
        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(threadsTotal, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }
}