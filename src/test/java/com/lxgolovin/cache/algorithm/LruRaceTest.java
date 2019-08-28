package com.lxgolovin.cache.algorithm;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LruRaceTest {

    private static final int THREADS_TOTAL = 100;

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(THREADS_TOTAL);

    private final CacheAlgorithm<Integer> lQueue = new Lru<>();

    @Test
    void shiftDataStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        latch.countDown();
                        latch.await();
                        lQueue.shift(elem);
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertTrue(lQueue.pop().isPresent());
    }

    @Test
    void shiftOneElementManyTimes() throws InterruptedException, ExecutionException {
        final Integer element = 5000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS_TOTAL; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                lQueue.shift(element);
                Thread.yield();
                lQueue.pop();
                Thread.yield();
                lQueue.shift(element);
            }, EXEC));
        }

        FutureConverter.getAllFinished(futures).get();
        assertTrue(lQueue.delete(element));
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(THREADS_TOTAL);

        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        latch.countDown();
                        latch.await();

                        lQueue.shift(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        lQueue.delete(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        lQueue.shift(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertTrue(lQueue.pop().isPresent());
    }

    @AfterAll
    static void tearDown() {
        EXEC.shutdown();
    }

}
