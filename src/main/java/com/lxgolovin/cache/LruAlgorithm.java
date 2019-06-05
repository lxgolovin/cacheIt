package com.lxgolovin.cache;
// TODO: To be documented

/**
 *
 * @param <E>
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
    protected void updatePeak() {
        peak = queue.keySet().stream().findFirst().orElse(null);
    }
}