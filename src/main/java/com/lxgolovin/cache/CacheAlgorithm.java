package com.lxgolovin.cache;
// TODO: To be documented

/**
 *
 * @param <E>
 */
public interface CacheAlgorithm<E> {

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - to be inserted
     * @return
     */
    E shift(E elem);

    /**
     * Deletes element from the queue depending on the algorithm type
     * @return if success, returns element, that was removed from the queue
     */
    E delete();

    /**
     * Deletes defined element from the queue
     * @return the element deleted
     */
    E delete(E elem);

    /**
     * Flashes {@link AbstractRuAlgorithm#queue}
     * All elements are deleted
     */
    void flash();

    /**
     * @return type of used algorithm
     */
    String getType();
}
