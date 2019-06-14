package com.lxgolovin.set;

import java.util.*;

/**
 * Implements doubly linked set based on {@link HashMap}. The elements link each other
 * by access order. This set is a kind of implementation of LinkedHashMap with access order
 * set to true
 * @param <E>
 */
public class AccessHashSet<E> extends AbstractSet<E> implements Set<E> {

    /**
     * Map to keep elements
     */
    private final Map<E,Object> map;

    /**
     * Dummy value to associate with an Object in the backing Map
     */
    private static final Object DUMMY = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public AccessHashSet() {
        map = new HashMap<>();
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * If this set already contains the element, the call leaves rearranges
     * the set in access order and returns false
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    @Override
    public boolean add(E e) {
        //TODO: implement access order
        return map.put(e, DUMMY)==null;
    }

    /**
     * Returns an iterator over the elements in this set.
     * TODO: The elements should be returned in particular order.
     *
     * @return an Iterator over the elements in this set
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality)
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
