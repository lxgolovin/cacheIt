package com.lxgolovin.cache.algorithm;

import com.lxgolovin.cache.tools.FutureConvertor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentLruTest {

    private static final int threadsTotal = 100;

    private final CacheAlgorithm<Integer> lQueue = new Lru<>();

    private static ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);


    @Test
    void shiftDataStressTest() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadsTotal);

        IntStream.rangeClosed(1,threadsTotal)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        latch.countDown();
                        latch.await();
                        lQueue.shift(elem);
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, exec)));

        FutureConvertor.listToFuture(futures).get();
        assertTrue(lQueue.pop().isPresent());
    }

    @Test
    void shiftOneElementManyTimes() throws InterruptedException, ExecutionException {
        final Integer element = 5000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadsTotal; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                lQueue.shift(element);
                Thread.yield();
                lQueue.pop();
                lQueue.shift(element);
            }, exec));
        }

        FutureConvertor.listToFuture(futures).get();
        assertTrue(lQueue.delete(element));
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }

}
