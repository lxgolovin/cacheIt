package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.Storage;
import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

    private static final CacheAlgorithm<Integer> lru = new Lru<>();

    private static final int maxSize = 40;

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private static Cache<Integer, String> lruCache;

    private static final String directoryPath = "./TEMP/";

    @BeforeAll
    static void setUp() {
        final Storage<Integer, String> lruStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        lruCache = new CacheLevel<>(lru, lruStorage, maxSize);

        for (int i = 0; i < maxSize; i++) {
            lruCache.cache(i, "init");
        }
    }

    @Test
    void putDataIntoCacheSleep() throws InterruptedException {
        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i ->
                        exec.execute(() -> {
                                    List<Integer> data = generateList();
                                    data.forEach(k -> {
                                        String v = String.valueOf(Math.random() * threadsTotal);
                                        lruCache.cache(k, v);
                                    });
                                }
                        ));

        TimeUnit.SECONDS.sleep(3); // wait all finished
        assertEquals(maxSize, lruCache.size());
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

                            lruCache.clear();
                            assertTrue(lruCache.size() >= 0);
                            Thread.yield();

                            lruCache.cache(k, v);
                            Thread.yield();
                            lruCache.delete(k);
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
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

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }
}