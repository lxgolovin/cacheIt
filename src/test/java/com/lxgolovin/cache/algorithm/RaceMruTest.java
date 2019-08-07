package com.lxgolovin.cache.algorithm;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceMruTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private final CacheAlgorithm<Integer> mQueue = new Mru<>();

    @Test
    void shiftDataStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        IntStream.rangeClosed(1,threadsTotal)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        latch.countDown();
                        latch.await();
                        mQueue.shift(elem);
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertTrue(mQueue.pop().isPresent());
    }

    @Test
    void shiftOneElementManyTimes() throws InterruptedException, ExecutionException {
        final Integer element = 5000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadsTotal; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                mQueue.shift(element);
                Thread.yield();
                mQueue.pop();
                Thread.yield();
                mQueue.shift(element);
            }, exec));
        }

        FutureConverter.getAllFinished(futures).get();
        assertTrue(mQueue.delete(element));
    }

    @Test
    void chaosStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        IntStream.rangeClosed(1,threadsTotal)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        latch.countDown();
                        latch.await();

                        mQueue.shift(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        mQueue.delete(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        mQueue.shift(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConverter.getAllFinished(futures).get();
        assertTrue(mQueue.pop().isPresent());
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }

}
