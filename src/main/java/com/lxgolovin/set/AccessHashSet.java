package com.lxgolovin.set;

import java.util.*;

/**
 * Implements doubly linked set based on {@link HashMap}. The elements link each other
 * by access order. This set is a kind of implementation of LinkedHashMap with access order
 * set to true
 * @param <E>
 */
public class AccessHashSet<E> {

    /**
     * Map to keep elements
     */
    private final Map<E,Node<E>> map;

    /**
     * Dummy value to associate with an Object in the backing Map
     */
    private static final Object DUMMY = new Object();

    private class Node<K> {
        K next, prev;

        Node(K next, K prev) {
            this.next = next;
            this.prev = prev;
        }
    }

    private E head;
    private E tail;

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
     * @param elem element to be added to this set
     * @return <tt>true</tt> if this set did already contain the specified
     * element
     */
    public boolean put(E elem) {
        Node<E> newNode = new Node<>(null, null);

        //TODO: implement access order
        if (map.isEmpty()) {
            head = elem;
            tail = elem;
        } else if (!map.containsKey(elem)) {
            map.get(tail).next = elem;
            newNode.prev = tail;
            tail = elem;
        } else {
            return poke(elem);
        }
        return map.put(elem, newNode) != null;
    }

    private boolean poke(E elem) {
        if (!map.containsKey(elem)) {
            return false;
        }

        Node<E> pokedNode = map.get(elem);

        // if poke the tail - nothing need to do, just return true. So check if poked not tail
        if (pokedNode.next != null) {
            // poke the head
            if (pokedNode.prev == null) {
                head = pokedNode.next;
                map.get(head).prev = null;
            } else { // poke the middle
                // relink items between
                Node<E> nextNode = map.get(pokedNode.next);
                Node<E> prevNode = map.get(pokedNode.prev);
                nextNode.prev = pokedNode.prev;
                prevNode.next = pokedNode.next;
            }

            // rearrange poked node
            map.get(tail).next = elem;
            pokedNode.prev = tail;
            pokedNode.next = null;
            tail = elem;
        }
        return true;
    }

    /**
     * Removes the specified element from this set if it is present.
     * Returns <tt>true</tt> if this set contained the element.
     * TODO: The order of the elements should be rearranged after deleting
     *
     * @param e element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(E e) {
        return map.remove(e) == DUMMY;
    }

    /**
     * Removes the first element from the set and returns deleted element
     * @return cut head
     */
    public E cutHead() {
        return null;
    }

    /**
     * Removes the lest element from the set and returns deleted element
     * @return cut tail
     */
    public E cutTail() {
        return null;
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
