package com.lxgolovin.cache.storage;

import com.lxgolovin.cache.tools.FutureConvertor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentMemoryStorageTest {

    private static Logger logger = LoggerFactory.getLogger(ConcurrentMemoryStorageTest.class);

    private static final int threadsTotal = 5;

    private static ExecutorService exec = Executors.newFixedThreadPool(threadsTotal);

    private static Map<Integer, String> map = new HashMap<>();

    private final Storage<Integer, String> storage = new MemoryStorage<>();

    @BeforeAll
    static void setUp() {
        for (int i = 0; i < threadsTotal; i++) {
            map.put(i, String.valueOf(Math.random() * 100));
        }
    }

    @Test
    void putKeyToHashMapSleep() throws InterruptedException {
        map.forEach((k, v) -> exec.execute(() -> {
            storage.put(k, v);
            logger.info("Sleep inserted {}, {}", k, v);
        }));

        TimeUnit.SECONDS.sleep(3); // wait all finished
        assertEquals(threadsTotal, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @Test
    void putKeyToHashMapFuture() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        map.forEach((k, v) -> futures.add(CompletableFuture.runAsync(() -> {
            storage.put(k, v);
            logger.info("Future inserted {}, {}", k, v);
        }, exec)));

        FutureConvertor.getAllFinished(futures).get();
        assertEquals(threadsTotal, storage.size());
        map.forEach((k, v) -> {
            assertTrue(storage.containsKey(k));
            assertTrue(storage.get(k).isPresent());
            assertEquals(Optional.of(v), storage.get(k));
        });
    }

    @Test
    void putKeyToHashMapLatch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(threadsTotal);

        map.forEach((k, v) -> exec.execute(() -> {
            try {
                logger.info("Latch ready: {}, {}", k, v);
                latch.countDown();
                latch.await();
                storage.put(k, v);
                logger.info("Latch inserted {}, {}", k, v);
            } catch (InterruptedException e) {
                logger.info("Not inserted {}, {}, {}", k, v, e);
            }
        }));

        TimeUnit.SECONDS.sleep(3); // wait all finished
        assertEquals(threadsTotal, storage.size());
        assertEquals(storage.getAll(), map);
    }

    @AfterAll
    static void tearDown() {
        exec.shutdown();
    }
}