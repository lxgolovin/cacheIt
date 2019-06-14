package com.lxgolovin.set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create checks-tests for the {@link Set} implementation {@link AccessHashSet}
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
        // Element 1 is present, so true
        assertTrue(set.put(1));
        // Element 5 is not present, should be false
        assertFalse(set.put(5));
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