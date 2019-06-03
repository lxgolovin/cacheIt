package com.lxgolovin.cache;
// TODO: To be documented

/**
 *
 * @param <E>
 */
public class LruAlgorithm<E> extends AbstractRuAlgorithm<E>
        implements CacheAlgorithm<E> {

    /**
     *
     */
    public LruAlgorithm() {
        super();
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public E shift(E elem) {
        return super.shift(elem);
    }

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
    protected void updatePeak() {
        peak = queue.keySet().stream().findFirst().orElse(null);
    }
}