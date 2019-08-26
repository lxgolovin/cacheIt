package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Mru;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheLevelFsMruRaceTest {

    private static final int THREADS_TOTAL = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(THREADS_TOTAL);

    private final CacheAlgorithm<Integer> mru = new Mru<>();

    private final int dataSize = 100;

    private final int maxSize = 40;

    private Cache<Integer, String> mruCache;

    @BeforeEach
    void setUp() {
        final String directoryPath = "./TEMP/";
        final Storage<Integer, String> mruStorage = new FileSystemStorage<>(Paths.get(directoryPath), true);
        mruCache = new CacheLevel<>(mru, mruStorage, maxSize);

        for (int i = 0; i < maxSize; i++) {
            mruCache.cache(i, "init");
        }
    }

    @Test
    void putDataIntoCacheBarrier() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier barrier = new CyclicBarrier(THREADS_TOTAL + 1, () -> {
            assertEquals(maxSize, mruCache.size());
            assertEquals(mruCache.sizeMax(), mruCache.size());
        });

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> exec.execute(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * THREADS_TOTAL);
                            mruCache.cache(k, v);
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

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i ->
                        futures.add(CompletableFuture.runAsync(() -> {
                            List<Integer> data = ListGenerator.generateInt(dataSize);
                            data.forEach(k -> {
                                String v = String.valueOf(Math.random() * THREADS_TOTAL);
                                mruCache.cache(k, v);
                            });
                        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(maxSize, mruCache.size());
    }

    @Test
    void putDataIntoCacheLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * THREADS_TOTAL);
                            mruCache.cache(k, v);
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(maxSize, mruCache.size());
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * THREADS_TOTAL);
                            mruCache.pop();
                            Thread.yield();

                            mruCache.cache(k, v);
                            Thread.yield();

                            mruCache.get(k);
                            Thread.yield();

                            assertTrue(maxSize >= mruCache.size());
                            mruCache.delete(k);
                            Thread.yield();

                            mruCache.cache(k, v);
                            assertTrue(maxSize >= mruCache.size());
                        });
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertTrue(maxSize >= mruCache.size());
    }

    @AfterEach
    void tearDown() {
        mruCache.clear();
        assertEquals(0, mruCache.size());
    }

    @AfterAll
    static void finish() {
        exec.shutdown();
    }
}