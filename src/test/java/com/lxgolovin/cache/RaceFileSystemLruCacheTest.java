package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.Storage;
import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceFileSystemLruCacheTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private final CacheAlgorithm<Integer> lru = new Lru<>();

    private final int maxSize = 40;

    private Cache<Integer, String> lruCache;

    @BeforeEach
    void setUp() {
        final String directoryPath = "./TEMP/";
        final Storage<Integer, String> lruStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        lruCache = new CacheLevel<>(lru, lruStorage, maxSize);

        for (int i = 0; i < maxSize; i++) {
            lruCache.cache(i, "init");
        }
    }

    @Test
    void putDataIntoCacheFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i ->
                        futures.add(CompletableFuture.runAsync(() -> {
                            List<Integer> data = generateList();
                            data.forEach(k -> {
                                String v = String.valueOf(Math.random() * threadsTotal);
                                lruCache.cache(k, v);
                            });
                        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(maxSize, lruCache.size());
    }

    @Test
    void putDataIntoCacheLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = generateList();
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * threadsTotal);
                            lruCache.cache(k, v);
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(maxSize, lruCache.size());
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = generateList();
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * threadsTotal);
                            lruCache.pop();
                            Thread.yield();

                            lruCache.cache(k, v);
                            Thread.yield();

                            lruCache.get(k);
                            Thread.yield();

                            assertTrue(maxSize >= lruCache.size());
                            lruCache.delete(k);
                            Thread.yield();

                            lruCache.cache(k, v);
                            assertTrue(maxSize >= lruCache.size());
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.listToFuture(futures).get();
        assertTrue(maxSize >= lruCache.size());
    }

    private List<Integer> generateList() {
        final int dataSize = 100;
        return IntStream
                .rangeClosed(1, dataSize)
                .map(i -> (int)(Math.random() * threadsTotal))
                .boxed()
                .collect(Collectors.toList());
    }

    @AfterEach
    void tearDown() {
        lruCache.clear();
        assertEquals(0, lruCache.size());
    }

    @AfterAll
    static void finishAll() {
        exec.shutdown();
    }
}