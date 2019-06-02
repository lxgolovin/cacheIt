package com.lxgolovin.cache;

import java.util.*;
import java.util.Map;

public class LruMru<I> implements CacheAlgorithm<I> {

    /**
     * LRU algorithm
     */
    private static final String LRU_ALGORITHM = "LRU";

    /**
     * MRU algorithm
     */
     private static final String MRU_ALGORITHM = "MRU";

    /**
     * Default algorithm type if none is defined in constructor
     */
    private static final String DEFAULT_ALGORITHM_TYPE = LRU_ALGORITHM;

    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Dummy value to associate with an Object in the backing Map
     */
    private static final Object DUMMY = new Object();

    /**
     * Current algorithm type
     */
    private String algo = DEFAULT_ALGORITHM_TYPE;

    /**
     * The youngest element in queue
     */
    private I tail;

    /**
     *  The eldest element in queue
     */
    private I head;

    /**
     * Queue to organize algorithm
     */
    private Map<I, Object> queue;

    public LruMru() {
        queue = new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        tail = head = null;
    }

    public LruMru(String algorithm) {
        queue = new LinkedHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
        if ( MRU_ALGORITHM.equals(algorithm) ) {
            algo = algorithm;
        } else { algo = LRU_ALGORITHM; }
        tail = head = null;
    }

    /**
     * Adds new element to the queue or renews elements order if it is already present in queue
     * @param elem - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public I shift(I elem) {
        if ( elem == null ) { throw new IllegalArgumentException(); }

        queue.put(elem, DUMMY);
        tail = elem;
        if (head == null) { head = elem; }
        else if ( head == elem ) { updateHead(); }

        return elem;
    }

    /**
     * Deletes element from the queue depending on the algorithm type
     * If success, returns element, that was removed from the queue
     * Returns null if queue is empty
     */
    @Override
    public I delete() {
        I elem = null;

        if ( queue.size() > 0 ) {
            switch (algo) {
                case LRU_ALGORITHM:
                    queue.remove(head);
                    elem = head;
                    updateHead();
                    break;
                case MRU_ALGORITHM:
                    queue.remove(tail);
                    elem = tail;
                    updateTail();
                    break;
            }
        }
        return elem;
    }

    /**
     * Removes element from the queue
     * @param elem - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public I delete(I elem) {
        if ( elem == null ) { throw new IllegalArgumentException(); }

        queue.remove(elem);
        if ( elem == head ) { updateHead(); }
        if ( elem == tail ) { updateTail(); }

        return elem;
    }

    @Override
    public String toString() {
        return algo;
    }

    /**
     * @return type of used algorithm
     */
    @Override
    public String getType() {
        return toString();
    }

    @Override
    public void flash() {
        queue.clear();
    }

    private void updateTail() {
        if (queue.size() > 0) {
            List<I> list = new ArrayList<>(queue.keySet());
            tail = list.get(list.size()-1);
        } else {
            tail = null;
        }
        if ( tail == null ) head = null;
    }

    private void updateHead() {
        head = queue.keySet().stream().findFirst().orElse(null);
        if ( head == null ) tail = null;
    }
}
