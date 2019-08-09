package com.lxgolovin.cache;

import com.lxgolovin.cache.algorithm.CacheAlgorithm;
import com.lxgolovin.cache.algorithm.Mru;
import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceMemoryMruCacheTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private final CacheAlgorithm<Integer> mru = new Mru<>();

    private final int maxSize = 40;

    private Cache<Integer, String> mruCache;

    @BeforeEach
    void setUp() {
        mruCache = new CacheLevel<>(mru, maxSize);
        for (int i = 0; i < maxSize; i++) {
            mruCache.cache(i, "init");
        }
    }

    @Test
    void putDataIntoCacheSleep() throws InterruptedException {
        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i ->
                        exec.execute(() -> {
                            List<Integer> data = generateList();
                            data.forEach(k -> {
                                        String v = String.valueOf(Math.random() * threadsTotal);
                                        mruCache.cache(k, v);
                                    });
                                }
                        ));

        TimeUnit.SECONDS.sleep(3); // wait all finished
        assertTrue(maxSize >= mruCache.size());
    }

    @Test
    void putDataIntoCacheFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i ->
                        futures.add(CompletableFuture.runAsync(() -> {
                            List<Integer> data = generateList();
                            data.forEach(k -> {
                                String v = String.valueOf(Math.random() * threadsTotal);
                                mruCache.cache(k, v);
                            });
                        }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(maxSize, mruCache.size());
    }

    @Test
    void putDataIntoCacheLatch() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = generateList();
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * threadsTotal);
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
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        assertEquals(maxSize, mruCache.size());
        IntStream.rangeClosed(1,threadsTotal)
                .forEach(i -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        List<Integer> data = generateList();
                        latch.countDown();
                        latch.await();
                        data.forEach(k -> {
                            String v = String.valueOf(Math.random() * threadsTotal);
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
        mruCache.clear();
        assertEquals(0, mruCache.size());
    }

    private List<Integer> generateList() {
        final int dataSize = 1000;
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