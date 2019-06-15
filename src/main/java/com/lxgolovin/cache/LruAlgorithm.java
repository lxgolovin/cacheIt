package com.lxgolovin.cache;

/**
 * Implementation of abstract class {@link AbstractRuAlgorithm} with methods to define
 * Least Recently Used (LRU) algorithms.
 * Implements {@link CacheAlgorithm} using as a queue of keys.
 * Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see CacheAlgorithm
 * @see AbstractRuAlgorithm
 * @see MruAlgorithm
 */
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
     * Deletes element from the queue depending on the LRU algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    @Override
    public E pop () {
        return queue.cutHead();
    }
}