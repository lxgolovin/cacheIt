package com.lxgolovin.set;

import java.util.*;

/**
 * Implements doubly linked "set" based on {@link HashMap}. The elements link each other
 * by access order. This set is a kind of implementation of LinkedHashMap with access order
 * set to true
 * @param <E> type for the incoming element
 */
public class AccessHashSet<E> {

    /**
     * Map to keep elements
     */
    private final Map<E,Node<E>> map;

    /**
     * Inner class to define values inside map
     * The class is a structure to get next and previous element
     * Both elements are <K> type
     *
     * @param <K> links to next and previous elements
     */
    private final class Node<K> {

        /**
         * Pointers to previous and next element
         */
        private K next, prev;

        /**
         * Constructor to set up values
         * @param next element
         * @param prev element
         */
        Node(K next, K prev) {
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * Pointer to the first element
     * Actually it is LRU
     */
    private E head;

    /**
     * Pointer to the last element in map
     * Actually it is MRU
     */
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
     * If this set already contains the element, the call rearranges
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
        return (map.put(elem, newNode) != null);
    }

    /**
     * Method to poke the element is it is present in the set
     * Actaully it works as access ordering algorithm. If the element poked
     * it moves to the tail. At the same time tail/head are updated if needed.
     * Also links between elements are updated if the element is poked from the middle
     *
     * @param elem poked element
     * @return false if element is not in the map, else true
     */
    private boolean poke(E elem) {
        if (!map.containsKey(elem)) {
            return false;
        }

        Node<E> pokedNode = map.get(elem);

        // if poke the tail - nothing need to do, just return true.
        // So check if poked not tail
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
        return (map.remove(e) != null);
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
