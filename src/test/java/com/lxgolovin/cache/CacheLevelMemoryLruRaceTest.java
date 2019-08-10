package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Lru;
import com.lxgolovin.cache.tools.FutureConverter;
import com.lxgolovin.cache.tools.ListGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class CacheLevelMemoryLruRaceTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private final CacheAlgorithm<Integer> lru = new Lru<>();

    private final int dataSize = 1000;

    private final int maxSize = 40;

    private Cache<Integer, String> lruCache;

    @BeforeEach
    void setUp() {
        lruCache = new CacheLevel<>(lru, maxSize);
        for (int i = 0; i < maxSize; i++) {
            lruCache.cache((-1)*i, "init");
        }
    }

    @Test
    void putDataIntoCacheBarrier() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier barrier = new CyclicBarrier(threadsTotal + 1, () -> {
            assertEquals(maxSize, lruCache.size());
            assertEquals(lruCache.sizeMax(), lruCache.size());
        });

        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i -> exec.execute(() -> {
                    try {
                        List<Integer> data = ListGenerator.generateInt(dataSize);
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * threadsTotal);
                            lruCache.cache(k, v);
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

        assertEquals(maxSize, lruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i ->
                        futures.add(CompletableFuture.runAsync(() -> {
                            List<Integer> data = ListGenerator.generateInt(dataSize);
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
                        List<Integer> data = ListGenerator.generateInt(dataSize);
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
                        List<Integer> data = ListGenerator.generateInt(dataSize);
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

        FutureConverter.getAllFinished(futures).get();
        assertTrue(maxSize >= lruCache.size());
    }

    @AfterEach
    void tearDown() {
        lruCache.clear();
        assertEquals(0, lruCache.size());
    }

    @AfterAll
    static void finish() {
        exec.shutdown();
    }
}