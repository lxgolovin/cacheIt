package com.lxgolovin.cache;

import java.util.*;

/**
 * Implementation of abstract class {@link AbstractRuAlgorithm} with methods to define
 * Most Recently Used (MRU) algorithms.
 * Implements {@link CacheAlgorithm} using as a queue of keys with
 * dummy objects as values. Specifies main methods to add values to the queue and delete them
 * if needed. Has a possibility to clear all data and check the algorithm name
 * @param <E>
 * @see CacheAlgorithm
 * @see AbstractRuAlgorithm
 * @see LruAlgorithm
 */
//! measure the complexity
public class MruAlgorithm<E> extends AbstractRuAlgorithm<E>
        implements CacheAlgorithm<E> {

    /**
     * @return type of used algorithm
     */
    @Override
    public String getType() {
        return MRU_ALGORITHM;
    }

    /**
     * Deletes element from the queue depending on the MRU algorithm type
     * @return element that was deleted or null if the queue is empty
     */
    @Override
    public E pop () {
        //! Much better: isEmpty()
        if (queue.size() < 1) {
            return null;
        }
        List<E> list = new ArrayList<>(queue.keySet());
        E elem =list.get(list.size()-1);
        queue.remove(elem);
        return elem;
    }
}