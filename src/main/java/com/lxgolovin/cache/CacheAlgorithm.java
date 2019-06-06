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
    //!+ What does it return?
    void shift(E elem);

    /**
     * Deletes element from the queue depending on the algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    //! if not success Exception?
    E pop();

    /**
     * Removes element from the queue
     * @param elem element to be deleted
     */
    void delete(E elem);

    /**
     * Clears all data from the queue
     * All elements are deleted
     */
    //! why flash? flush? clearAll? clear?
    void clearAll();

    /**
     * @return type of used algorithm
     */
    String getType();
}
