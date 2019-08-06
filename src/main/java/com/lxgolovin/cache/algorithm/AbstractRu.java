package com.lxgolovin.cache.algorithm;

import com.lxgolovin.cache.core.AccessHashSet;

import java.util.Optional;

/**
 * Abstract class with some methods to define Recently Used (LRU-MRU) algorithms.
 * Implements {@link CacheAlgorithm} using {@link AccessHashSet} as a queue of keys
 * Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see Lru
 * @see Mru
 * @see AccessHashSet
 */
abstract class AbstractRu<E> implements CacheAlgorithm<E> {

    /**
     * LRU algorithm
     */
    static final String LRU_ALGORITHM = "LRU";

    /**
     * MRU algorithm
     */
    static final String MRU_ALGORITHM = "MRU";

    /**
     * Default algorithm type if none is defined in constructor
     */
    static final String DEFAULT_ALGORITHM_TYPE = "LRU";

    /**
     * Queue to organize algorithm
     */
    final AccessHashSet<E> queue;

    /**
     * Starts a queue to keep all elements inside and delete according to algorithms
     */
    AbstractRu() {
        queue = new AccessHashSet<>();
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @return true if element was present in queue, else false
     * @throws IllegalArgumentException if any of the params is null or if some property of
     *          the specified element prevents it from being stored in {@link AbstractRu#queue}
     */
    @Override
    public boolean shift(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        return (queue.put(elem));
    }

    /**
     * Deletes element from the queue depending on the algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    @Override
    public abstract Optional<E> pop();

    /**
     * Removes element from the queue
     * @param elem - may not be null
     * @return true if element was present in queue, else false
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public boolean delete(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        return (queue.remove(elem));
    }

    /**
     * @return returns toString for the object
     */
    @Override
    public String toString() {
        return getType();
    }

    /**
     * @return type of used algorithm
     */
    @Override
    public abstract String getType();

    /**
     * Clears all data from the queue {@link AbstractRu#queue}
     * All elements are deleted
     */
    @Override
    public void clear() {
        queue.clear();
    }
}