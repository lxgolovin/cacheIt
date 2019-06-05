package com.lxgolovin.cache;

/**
 * Implementation of abstract class {@link AbstractRuAlgorithm} with methods to define
 * Least Recently Used (LRU) algorithms.
 * Implements {@link CacheAlgorithm} using as a queue of keys with
 * dummy objects as values. Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see LruAlgorithm
 * @see MruAlgorithm
 */
//! Estimate the complexity of the algorithm
public class LruAlgorithm<E> extends AbstractRuAlgorithm<E>
        implements CacheAlgorithm<E> {

    /**
     * @return type of used algorithm
     */
    @Override
    public String getType() {
        return LRU_ALGORITHM;
    }

    /**
     *
     */
    @Override
    public E pop () {
        return queue.keySet().stream().findFirst().orElse(null);
    }
}