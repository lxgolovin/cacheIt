package com.lxgolovin.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract class with some methods to define Recently Used (LRU-MRU) algorithms.
 * Implements {@link CacheAlgorithm} using {@link LinkedHashMap} as a queue of keys with
 * dummy objects {@link AbstractRuAlgorithm#DUMMY} as values
 * Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see LruAlgorithm
 * @see MruAlgorithm
 */
abstract class AbstractRuAlgorithm<E> implements CacheAlgorithm<E> {

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
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Dummy value to associate with an Object in the backing Map
     */
    private static final Object DUMMY = new Object();

    /**
     * Queue to organize algorithm
     */
    final Map<E, Object> queue;

    /**
     * Starts a queue to keep all elements inside and delete according to algorithms
     */
    AbstractRuAlgorithm() {
        queue = new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @return true if element was present in queue, else false
     * @throws IllegalArgumentException if any of the params is null or if some property of
     *          the specified element prevents it from being stored in {@link AbstractRuAlgorithm#queue}
     */
    @Override
    public boolean shift(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        return (queue.put(elem, DUMMY) != null);
    }

    /**
     * Deletes element from the queue depending on the algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    @Override
    public abstract E pop();

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
        return (queue.remove(elem) != null);
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
     * Clears all data from the queue {@link AbstractRuAlgorithm#queue}
     * All elements are deleted
     */
    @Override
    public void clear() {
        queue.clear();
    }
}
