package com.lxgolovin.cache.algorithm;

import com.lxgolovin.cache.core.AccessHashSet;

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

    static final String LRU_ALGORITHM = "LRU";

    static final String MRU_ALGORITHM = "MRU";

    static final String DEFAULT_ALGORITHM_TYPE = "LRU";

    final AccessHashSet<E> queue;

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
            throw new IllegalArgumentException("elem cannot be null");
        }
        return (queue.put(elem));
    }

    /**
     * Removes element from the queue
     * @param elem - may not be null
     * @return true if element was present in queue, else false
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public boolean delete(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException("elem cannot be null");
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
     * Clears all data from the queue {@link AbstractRu#queue}
     * All elements are deleted
     */
    @Override
    public void clear() {
        queue.clear();
    }
}