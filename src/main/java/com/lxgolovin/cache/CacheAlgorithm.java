package com.lxgolovin.cache;

/**
 * Defined cache algorithm. Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see AbstractRuAlgorithm
 * @see LruAlgorithm
 * @see MruAlgorithm
 */
public interface CacheAlgorithm<E> {

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     */
    void shift(E elem);

    /**
     * Deletes element from the queue depending on the algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    E pop();

    /**
     * Removes element from the queue
     * @param elem element to be deleted
     * @return true if element was present in queue, else false
     */
    boolean delete(E elem);

    /**
     * Clears all data from the queue
     * All elements are deleted
     */
    void clear();

    /**
     * @return type of used algorithm
     */
    String getType();
}
