package com.lxgolovin.cache.set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create checks-tests for the {@link AccessHashSet}
 *
 * @see Set
 * @see AccessHashSet
 */
class AccessHashSetTest {

    /**
     * Defining the set to be tested
     */
    private final AccessHashSet<Integer> set = new AccessHashSet<>();

    /**
     * Initializes the set with first values
     * {1,2,3,4}
     */
    @BeforeEach
    void setUp() {
        IntStream.rangeClosed(1, 4).forEach(set::put);
    }

    /**
     * Simple check for adding elements into set
     */
    @Test
    void addElements() {
        // Element 2 is present, so true
        assertTrue(set.put(2));
        // Element 5 is not present, should be false
        assertFalse(set.put(5));
        assertFalse(set.put(null));
    }

    /**
     * Simple tests to remove elements
     */
    @Test
    void removeElements() {
        // removing head. Init {1,2,3,4}
        assertTrue(set.remove(1));
        // now 2,3,4; Let's remove middle
        assertTrue(set.remove(3));
        // now got 2,4. Let's remove tail
        assertTrue(set.remove(4));
        // now remove element that is not present
        assertFalse(set.remove(5));
        assertFalse(set.remove(4));
        assertFalse(set.remove(null), "Passing null element in false");
        assertTrue(set.remove(2));
        assertTrue(set.isEmpty());
    }

    /**
     * Check for cutting head of the set
     * Imitation of LRU algorithm
     */
    @Test
    void cutHead() {
        // Init set is {1,2,3,4}. cutHead should remove 1
        assertEquals(1,set.cutHead().get());
        // now {2,3,4}
        assertTrue(set.put(2));
        // now {3,4,2}
        assertEquals(3,set.cutHead().get());
        assertEquals(4,set.cutHead().get());
        assertEquals(2,set.cutHead().get());
        assertFalse(set.cutHead().isPresent());
    }

    /**
     * Check for cutting tail of the set
     * Imitation of MRU algorithm
     */
    @Test
    void cutTail() {
        // Init set is {1,2,3,4}. cutTail should remove 4
        assertEquals(4,set.cutTail().get());
        // now {1,2,3}
        assertTrue(set.put(2));
        // now {1,3,2}
        assertEquals(2,set.cutTail().get());
        assertEquals(3,set.cutTail().get());
        assertEquals(1,set.cutTail().get());
        assertFalse(set.cutTail().isPresent());
    }

    /**
     * Checks size return for the set
     */
    @Test
    void size() {
        assertEquals(4, set.size());
        assertFalse(set.put(5)); // returns false as the element was not present
        assertEquals(5, set.size());
    }

    /**
     * Checks if set is empty or not
     */
    @Test
    void isEmpty() {
        assertFalse(set.isEmpty());
        assertTrue(set.remove(1));
        assertTrue(set.remove(2));
        assertTrue(set.remove(3));
        assertTrue(set.remove(4));
        assertTrue(set.isEmpty());
    }
}