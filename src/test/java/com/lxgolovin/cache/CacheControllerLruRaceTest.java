package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.storage.FileSystemStorage;
import com.lxgolovin.cache.storage.Storage;
import com.lxgolovin.cache.tools.FutureConverter;
import com.lxgolovin.cache.tools.ListGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CacheControllerLruRaceTest {

    private static final int THREADS_TOTAL = 100;

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(THREADS_TOTAL);

    private CacheController<Integer, Integer> cc;

    private final int dataSize = 100;

    private final int maxSize = 40;

    @BeforeEach
    void setUp() {
        final CacheAlgorithm<Integer> lruLev0 = new Lru<>();
        final Cache<Integer, Integer> cacheLevel0 = new CacheLevel<>(lruLev0, maxSize);
        cc = new CacheController<>(cacheLevel0);

        final CacheAlgorithm<Integer> lruLev1 = new Lru<>();
        final Cache<Integer, Integer> cacheLevel1 = new CacheLevel<>(lruLev1, maxSize);
        assertEquals(2,cc.addLevel(cacheLevel1));

        final String directoryPath = "./TEMP/";
        final CacheAlgorithm<Integer> lruLev2 = new Lru<>();
        final Storage<Integer, Integer> lruStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        final Cache<Integer, Integer> cacheLevel2 = new CacheLevel<>(lruLev2, lruStorage, maxSize);
        assertEquals(3,cc.addLevel(cacheLevel2));

        for (int i = 0; i < (maxSize * cc.levels()); i++) {
            cc.cache(i, 0);
        }
        assertEquals(cc.size(), cc.sizeMax());
    }

    @Test
    void putDataIntoCacheBarrier() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier barrier = new CyclicBarrier(THREADS_TOTAL + 1, () -> {
            assertEquals(maxSize * cc.levels(), cc.size());
            assertEquals(cc.sizeMax(), cc.size());
        });

        assertEquals(cc.size(), cc.sizeMax());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> EXEC.execute(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        data.forEach(k -> {
                            int v = (int)(Math.random() * THREADS_TOTAL);
                            cc.cache(k, v);
                        });

                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        // just skip. Not needed here
                    }
                }));

        barrier.await();
    }

    @Test
    void putDataIntoCacheFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        assertEquals((maxSize * cc.levels()), cc.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i ->
                        futures.add(CompletableFuture.runAsync(() -> {
                            List<Integer> data = ListGenerator.generateInt(dataSize);
                            data.forEach(k -> {
                                int v = (int)(Math.random() * THREADS_TOTAL);
                                cc.cache(k, v);
                            });
                        }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals((maxSize * cc.levels()), cc.size());
    }

    @Test
    void putDataIntoCacheLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        assertEquals(cc.sizeMax(), cc.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            int v = (int)(Math.random() * THREADS_TOTAL);
                            cc.cache(k, v);
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(cc.sizeMax(), cc.size());
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        assertEquals(maxSize * cc.levels(), cc.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            int v = (int)(Math.random() * THREADS_TOTAL);
                            cc.pop();
                            Thread.yield();

                            cc.cache(k, v);
                            Thread.yield();

                            cc.get(k);
                            Thread.yield();

                            assertTrue(cc.sizeMax() >= cc.size());
                            cc.delete(k);
                            Thread.yield();

                            cc.cache(k, v);
                            assertTrue(cc.sizeMax() >= cc.size());
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, EXEC)));

        FutureConverter.listToFuture(futures).get();
        assertTrue(cc.sizeMax() >= cc.size());
    }

    @AfterEach
    void tearDown() {
        cc.clear();
        assertEquals(0, cc.size());
    }

    @AfterAll
    static void finish() {
        EXEC.shutdown();
    }
}