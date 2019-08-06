package com.lxgolovin.cache.core;

import com.lxgolovin.cache.tools.FutureConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RaceAccessHashSetTest {

    private static final int threadsTotal = 100;

    private static final ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private final AccessHashSet<Integer> set = new AccessHashSet<>();

    @Test
    void putDataToSetFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        IntStream.rangeClosed(1,threadsTotal)
                .forEach(elem -> futures.add(CompletableFuture.runAsync(() -> set.put(elem), exec)));

        FutureConverter.listToFuture(futures).get();
        assertEquals(threadsTotal, set.size());
    }

    @Test
    void putOneElementDeleteOneElement() throws InterruptedException, ExecutionException {
        final Integer element = 5000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadsTotal; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                set.put(element);
                Thread.yield();
                set.remove(element);
                Thread.yield();
                set.put(element);
            }, exec));
        }

        FutureConverter.getAllFinished(futures).get();
        assertEquals(1, set.size());
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }
}