package com.lxgolovin.cache;
// TODO: To be documented

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @param <E>
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
    private final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The load factor used when none specified in constructor.
     */
    private final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Dummy value to associate with an Object in the backing Map
     */
    private final Object DUMMY = new Object();

    /**
     *  The eldest element in queue
     */
    protected E peak;

    /**
     * Queue to organize algorithm
     */
    protected Map<E, Object> queue;

    /**
     *
     */
    protected AbstractRuAlgorithm() {
        queue = new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        peak = null;
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public E shift(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }
        queue.put(elem, DUMMY);
        if (peak == null) {
            peak = elem;
        } else if (peak == elem) {
            updatePeak();
        }

        return elem;
    }

    /**
     * Deletes element from the queue depending on the algorithm type
     * If success, returns element, that was removed from the queue
     * Returns null if queue is empty
     */
    @Override
    public E delete() {
        return delete(peak);
    }

    /**
     * Removes element from the queue
     * @param elem - may not be null
     * @return returns element that was deleted
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public E delete(E elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }

        queue.remove(elem);
        if (elem == peak ) {
            updatePeak();
        }
        return elem;
    }

    /**
     *
     */
    protected abstract void updatePeak();

    /**
     *
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
     *
     */
    @Override
    public void flash() {
        queue.clear();
        peak = null;
    }
}
