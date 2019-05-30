package com.lxgolovin.cache;

import java.util.HashMap;
import java.util.Map;

public class LRU<K> implements CacheAlgorithm<K> {

    private Map<K, Long> lruMap;

    /**
     * The youngest element in Map
     */
    private K tail;

    /**
     *  The eldest element in Map
     */
    private K head;

    public LRU() {
        lruMap = new HashMap<>();
        tail = head = null;
    }

//    public LRU(K key) {
//        lruMap = new HashMap<>();
//        add(key);
//    }

    @Override
    public K head() {
        return head;
    }

    @Override
    public K tail() {
        return tail;
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null
     */
    @Override
    public K add(K key) {
        long value = System.nanoTime();
        if ( key != null ) {
            lruMap.put(key, value);
            tail = key;
            if (head == null) { head = key; }
            else if ( head == key ) { updateHead(); }
            return key;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link LRU#lruMap} does not contain the key
     */
    @Override
    public K renew(K key) {
        if ( ( key != null ) && lruMap.containsKey(key)  ) {
            add(key);
            return key;
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param key - may not be null
     * @throws IllegalArgumentException if any of the params is null or
     *          {@link LRU#lruMap} does not contain the key
     */
    @Override
    public K delete(K key) {
        if ( key != null && lruMap.containsKey(key)) {
            lruMap.remove(key);
            if (key == head) { updateHead(); }
            if (key == tail) { updateTail(); }
            return key;
        }
        throw new IllegalArgumentException();
    }

    private void updateTail() {
        tail = lruMap.entrySet().stream()
                .sorted(Map.Entry.<K,Long>comparingByValue().reversed())
                .map(x->x.getKey()).findFirst().orElse(null);
    }

    private void updateHead() {
        head = lruMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(x->x.getKey()).findFirst().orElse(null);
    }
}
