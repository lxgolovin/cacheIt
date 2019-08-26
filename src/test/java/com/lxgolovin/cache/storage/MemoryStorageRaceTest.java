package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryStorageRaceTest {

    private static final int THREADS_TOTAL = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(THREADS_TOTAL);

    private final Map<Integer, String> map = new HashMap<>();

    private final Storage<Integer, String> storage = new MemoryStorage<>();

    @BeforeEach
    void setUp() {
        for (int i = 0; i < THREADS_TOTAL; i++) {
            map.put(i, String.valueOf(Math.random() * THREADS_TOTAL * 10));
        }
    }

    @Test
    void putKeyToHashMapSleep() throws InterruptedException {
        map.forEach((k, v) ->
                exec.execute(() -> {
                    storage.put(k, v);
                    Thread.yield();
                    storage.remove(k);
                    Thread.yield();
                    storage.put(k, v);
                }));

        TimeUnit.SECONDS.sleep(2); // wait all finished
        assertEquals(THREADS_TOTAL, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @Test
    void putKeyToHashMapFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        map.forEach((k, v) ->
                futures.add(CompletableFuture.runAsync(() -> {
                    storage.put(k, v);
                    Thread.yield();
                    storage.remove(k);
                    Thread.yield();
                    storage.put(k, v);
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(THREADS_TOTAL, storage.size());
        map.forEach((k, v) -> {
            assertTrue(storage.containsKey(k));
            assertTrue(storage.get(k).isPresent());
            assertEquals(Optional.of(v), storage.get(k));
        });
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        map.forEach((k, v) -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                latch.countDown();
                latch.await();

                storage.put(k, v);
                TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                Thread.yield();

                storage.remove(k);
                TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                Thread.yield();

                storage.put(k, v);
                TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                Thread.yield();
            } catch (InterruptedException e) {
                // just skip it and finish
            }
        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(THREADS_TOTAL, storage.size());
        assertEquals(storage.getAll(), map);
        storage.clear();
        assertEquals(0, storage.size());
    }

    @Test
    void putKeyToHashMapLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        map.forEach((k, v) -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                latch.countDown();
                latch.await();
                storage.put(k, v);
                Thread.yield();
                storage.remove(k);
                Thread.yield();
                storage.put(k, v);
            } catch (InterruptedException e) {
                // just skip it and finish
            }
        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(THREADS_TOTAL, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }
}