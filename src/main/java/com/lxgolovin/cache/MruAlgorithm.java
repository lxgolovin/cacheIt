package com.lxgolovin.cache;

// TODO: To be documented
import java.util.*;
import java.util.Map;

/**
 *
 * @param <E>
 */
public class MruAlgorithm<E> extends AbstractRuAlgorithm<E>
        implements CacheAlgorithm<E> {

    /**
     *
     */
    public MruAlgorithm() {
        super();
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public E shift(E elem) {
        peak = super.shift(elem);
        return peak;
    }

    /**
     * @return type of used algorithm
     */
    @Override
    public String getType() {
        return MRU_ALGORITHM;
    }

    /**
     *
     */
    protected void updatePeak() {
        if (queue.size() > 0) {
            List<E> list = new ArrayList<>(queue.keySet());
            peak = list.get(list.size()-1);
        } else {
            peak = null;
        }
    }
}