package com.lxgolovin.set;

import java.util.*;

/**
 * Implements doubly linked "set" based on {@link HashMap}. The elements link each other
 * by access order. This set is a kind of implementation of LinkedHashMap with access order
 * set to true
 *
 * @param <E> type for the incoming element
 * @see HashMap
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
        private K nextElem, prevElem;

        /**
         * Constructor to set up values
         *
         * @param nextElement next element
         * @param prevElement previous element
         */
        Node(K nextElement, K prevElement) {
            this.nextElem = nextElement;
            this.prevElem = prevElement;
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

        if (map.isEmpty()) {
            head = elem;
            tail = elem;
        } else if (map.containsKey(elem)) {
            // if element is present, just poke the element and push to tail
            return poke(elem);
        } else {
            // if element is not in map, put the element to the tail
            Node<E> tailNode = map.get(tail);

            tailNode.nextElem = elem;
            newNode.prevElem = tail;
            tail = elem;
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
        if (isTail(pokedNode)) {
            return true;
        }

        // poke the head
        if (isHead(pokedNode)) {
            head = pokedNode.nextElem;
            map.get(head).prevElem = null;
        } else if (isMiddle(pokedNode)) {
            // poke the middle
            // relink items between
            Node<E> nextNode = map.get(pokedNode.nextElem);
            Node<E> prevNode = map.get(pokedNode.prevElem);
            nextNode.prevElem = pokedNode.prevElem;
            prevNode.nextElem = pokedNode.nextElem;
        }

        // rearrange poked node
        map.get(tail).nextElem = elem;
        pokedNode.prevElem = tail;
        pokedNode.nextElem = null;
        tail = elem;

        return true;
    }

    /**
     * Checks if the node-element is a tail
     * @param node to be checked
     * @return true if tail, else false
     */
    private boolean isTail(Node<E> node) {
        return (node.nextElem == null);
    }

    /**
     * Checks if the node-element is a head
     * @param node to be checked
     * @return true if the node is a head, else false
     */
    private boolean isHead(Node<E> node) {
        return (node.prevElem == null);
    }

    /**
     * Checks if the node is in the middle. If both next and prev elements are present
     * @param node to be checked
     * @return true if middle, else false
     */
    private boolean isMiddle(Node<E> node) {
        return ((node.nextElem != null) && (node.prevElem != null));
    }

    /**
     * Removes the specified element from this set if it is present.
     * Returns <tt>true</tt> if this set contained the element.
     * The order of the elements should be rearranged after deleting
     *
     * @param elem element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(E elem) {
        // the element moved to tail if it is present
        if (poke(elem)) {
            E beforeTail = map.get(elem).prevElem;
            if (beforeTail == null) { // the last element in the map
                head = null;
            } else { // this is not the last element in map
                map.get(beforeTail).nextElem = null;
            }
            tail = beforeTail;
        }

        return (map.remove(elem) != null);
    }

    /**
     * Removes the first element from the set and returns deleted element
     *
     * @return cut head
     */
    public E cutHead() {
        E elem = head;
        return (this.remove(head)) ? elem : null;
    }

    /**
     * Removes the lest element from the set and returns deleted element
     *
     * @return cut tail
     */
    public E cutTail() {
        E elem = tail;
        return (this.remove(tail)) ? elem : null;
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

    /**
     * Clears all data from the queue
     * All elements are deleted.
     */
    public void clear() {
        map.clear();
        head = null;
        tail = null;
    }
}
