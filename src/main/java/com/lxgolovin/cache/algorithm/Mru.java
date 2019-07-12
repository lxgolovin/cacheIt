package com.lxgolovin.cache.algorithm;

import java.util.Optional;

/**
 * Implementation of abstract class {@link AbstractRu} with methods to define
 * Most Recently Used (MRU) algorithms.
 * Implements {@link CacheAlgorithm} using as a queue of keys.
 * Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see CacheAlgorithm
 * @see AbstractRu
 * @see Lru
 */
public class Mru<E> extends AbstractRu<E>
        implements CacheAlgorithm<E> {

    /**
     * @return type of used algorithm
     */
    @Override
    public String getType() {
        return MRU_ALGORITHM;
    }

    /**
     * Deletes element from the queue depending on the MRU algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    @Override
    public Optional<E> pop () {
        return queue.cutTail();
    }
}