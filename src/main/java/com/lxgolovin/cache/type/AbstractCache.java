package com.lxgolovin.cache.type;

import com.lxgolovin.cache.Cache;
import com.lxgolovin.cache.algorithm.CacheAlgorithm;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Abstract implementation of interface {@link Cache}
 *
 * @param <K>
 * @param <V>
 * @see Cache
 * @see CacheAlgorithm
 */
public abstract class AbstractCache<K, V> implements Cache<K, V>  {

    /**
     * maximum possible size for the cache. Minimum value is greater then 1.
     * If you try to use less then 2, {@link Cache#DEFAULT_CACHE_SIZE}
     * will be used as a size
     */
    protected int maxSize;

    /**
     * Defines cache algorithm
     */
    protected CacheAlgorithm<K> algorithm;

    /**
     * Removes the mapping for a key from the cache by used algorithm.
     * To delete {@link Cache#delete(Object)} is used
     * @return popped out entry, returns null entry if the element was not
     *          found in algorithm queue (empty)
     */
    @Override
    public Map.Entry<K, V> pop() {
        K key = algorithm.pop();
        if (key == null) {
            return null;
        }

        Map.Entry<K, V> entry;
        V value = delete(key);
        entry = new AbstractMap.SimpleImmutableEntry<>(key, value);

        return entry;
    }

    /**
     * @return maximum possible size of the cache
     */
    @Override
    public int sizeMax() {
        return maxSize;
    }
}