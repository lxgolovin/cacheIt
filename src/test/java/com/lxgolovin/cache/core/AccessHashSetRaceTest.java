package com.lxgolovin.cache.core;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AccessHashSetRaceTest {

    private static final int THREADS_TOTAL = 100;

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(THREADS_TOTAL);

    private AccessHashSet<Integer> set;

    @BeforeEach
    void setUp() {
        set = new AccessHashSet<>();
    }

    @Test
    void putDataToSetFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        IntStream.rangeClosed(1, THREADS_TOTAL)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
                    set.put(elem);
                    Thread.yield();
                    set.remove(elem);
                    Thread.yield();
                    set.put(elem);
                }, EXEC)));

        FutureConverter.listToFuture(futures).get();
        assertEquals(THREADS_TOTAL, set.size());
    }

    @Test
    void puAndRemoveRaceConditions() throws InterruptedException, ExecutionException {
        final int puttersTotal = THREADS_TOTAL / 2;

        ArrayList<Integer> initData = new ArrayList<>();
        ArrayList<Integer> newData = new ArrayList<>();

        IntStream.rangeClosed(1,puttersTotal)
                .forEach(i -> {
                    initData.add(i);
                    set.put(i);
                    newData.add(i + puttersTotal);
                });

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(puttersTotal + puttersTotal);

        newData.forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                latch.countDown();
                latch.await();
                set.put(elem);
                TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                Thread.yield();
            } catch (InterruptedException e) {
                // just skip it and finish
            }
        }, EXEC)));

        initData.forEach(elem -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                latch.countDown();
                latch.await();
                set.remove(elem);
                TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                Thread.yield();
            } catch (InterruptedException e) {
                // just skip it and finish
            }
        }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(puttersTotal, set.size());
        newData.forEach(elem -> assertTrue(set.remove(elem)));
        assertTrue(set.isEmpty());
    }

    @Test
    void putOneElementDeleteOneElement() throws InterruptedException, ExecutionException {
        final Integer element = 5000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS_TOTAL; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                set.put(element);
                Thread.yield();
                set.remove(element);
                Thread.yield();
                set.put(element);
            }, EXEC));
        }

        FutureConverter.getAllFinished(futures).get();
        assertEquals(1, set.size());
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
                        set.put(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        set.remove(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();

                        set.put(elem);
                        TimeUnit.MILLISECONDS.sleep((int) (Math.random() * 100));
                        Thread.yield();
                    } catch (InterruptedException e) {
                        // just skip it and finish
                    }
                }, EXEC)));

        FutureConverter.getAllFinished(futures).get();
        assertEquals(THREADS_TOTAL, set.size());
    }

    @AfterAll
    static void tearDown() {
        EXEC.shutdown();
    }
}