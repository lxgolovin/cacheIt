package com.lxgolovin.cache.core;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements doubly linked "set" based on {@link ConcurrentHashMap}. The elements link each other
 * by access order. This set is a kind of implementation of LinkedHashMap with access order
 * set to true
 *
 * @param <E> type for the incoming element
 * @see ConcurrentHashMap
 */
@ThreadSafe
public class AccessHashSet<E> {

    /**
     * Map to keep elements
     */
    @GuardedBy("this")
    private final ConcurrentMap<E,Node<E>> map;

    @GuardedBy("this")
    private E head;

    @GuardedBy("this")
    private E tail;

    private final Lock lock = new ReentrantLock();

    /**
     * Inner class to define values inside map
     * The class is a structure to get next and previous element
     *
     * @param <K> links to next and previous elements
     */
    private static final class Node<K> {

        /**
         * Pointers to previous and next element
         */
        private K nextElem;
        private K prevElem;

        Node() {
            this.nextElem = null;
            this.prevElem = null;
        }
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public AccessHashSet() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * If this set already contains the element, the call rearranges
     * the set in access order and returns false
     *
     * @param elem element to be added to this set. elem cannot be null.
     * @return <tt>true</tt> if this set did already contain the specified
     *          element. Return false if elem is null
     */
    public boolean put(E elem) {
        if (elem == null) {
            return false;
        }

        lock.lock();
        try {
            // if element is present, just poke the element and push to tail
            if (map.containsKey(elem)) {
                return poke(elem);
            }

            Node<E> newNode;
            if (map.isEmpty()) {
                // create first element
                head = elem;
                tail = elem;
                newNode = new Node<>();
            } else {
                // if element is not in map, push the element to the tail
                newNode = linkElementToTail(elem);
            }

            return (map.put(elem, newNode) != null);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Method to poke the element if it is present in the set
     * Actually it works as access ordering algorithm. If the element poked
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

        // if poke the tail (the element is already tail) - nothing need to do, just return true.
        if (isTail(elem)) {
            return true;
        }

        if (isHead(elem)) {
            unlinkElementFromHead(elem);
        } else if (isMiddle(elem)) {
            unlinkElementFromMiddle(elem);
        }
        linkElementToTail(elem);

        return true;
    }

    /**
     * Removes the specified element from this set if it is present.
     * Returns <tt>true</tt> if this set contained the element.
     * The order of the elements should be rearranged after deleting
     *
     * @param elem element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     * @throws IllegalArgumentException if elem is null
     */
    public boolean remove(E elem) {
        if (elem == null) {
            return false;
        }

        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the first element from the set and returns deleted element
     *
     * @return cut head
     */
    public Optional<E> cutHead() {
        lock.lock();
        try {
            E elem = head;
            return (this.remove(head)) ? Optional.ofNullable(elem) : Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the lest element from the set and returns deleted element
     *
     * @return cut tail
     */
    public Optional<E> cutTail() {
        lock.lock();
        try {
            E elem = tail;
            return (this.remove(tail)) ? Optional.ofNullable(elem) : Optional.empty();
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            map.clear();
            head = null;
            tail = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if the element is a tail
     * @param elem to be checked
     * @return true if tail, else false
     */
    private boolean isTail(E elem) {
        Node<E> node = map.get(elem);
        return ((node.nextElem == null) & elem.equals(tail));
    }

    /**
     * Checks if the element is a head
     * @param elem to be checked
     * @return true if the node is a head, else false
     */
    private boolean isHead(E elem) {
        Node<E> node = map.get(elem);
        return ((node.prevElem == null) & elem.equals(head));
    }

    /**
     * Checks if the node is in the middle. If both next and prev elements are present
     * @param elem to be checked
     * @return true if middle, else false
     */
    private boolean isMiddle(E elem) {
        Node<E> node = map.get(elem);
        return ((node.nextElem != null) && (node.prevElem != null));
    }

    /**
     * This method links existing element or creates new and also link it
     * as a tail element. The method returns new node with new links. After this method
     * is finished current element becomes a tail of the map
     *
     * @param elem element to create ne node with links
     * @return the node with relinked pointers to next and previous elements.
     */
    private Node<E> linkElementToTail(E elem) {
        Node<E> pokedNode;

        if (map.containsKey(elem)) {
            // if the element is present, link to tail
            pokedNode = map.get(elem);
        } else {
            // if no such element in map, create empty and link to tail
            pokedNode = new Node<>();
        }

        Node<E> tailNode = map.get(tail);
        tailNode.nextElem = elem;
        pokedNode.prevElem = tail;
        pokedNode.nextElem = null;
        tail = elem;

        return pokedNode;
    }

    /**
     * If the element is a middle, it is unlinked from the middle.
     * The idea is, that previous and next elements after the method should link each other.
     * At the same time, current element becomes unlinked from the map!
     *
     * @param elem to be unlinked from the middle of the map
     */
    private void unlinkElementFromMiddle(E elem) {
        Node<E> pokedNode = map.get(elem);

        Node<E> nextNode = map.get(pokedNode.nextElem);
        Node<E> prevNode = map.get(pokedNode.prevElem);

        nextNode.prevElem = pokedNode.prevElem;
        prevNode.nextElem = pokedNode.nextElem;
    }

    /**
     * If the element is a head, it is unlinked from the head.
     * The idea is, that next element after the method should unlink the current.
     * At the same time, current element becomes unlinked from the map!
     *
     * @param elem to be unlinked from the middle of the map
     */
    private void unlinkElementFromHead(E elem) {
        Node<E> pokedNode = map.get(elem);

        head = pokedNode.nextElem;
        map.get(head).prevElem = null;
    }
}
