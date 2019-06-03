package com.lxgolovin.cache;
// TODO: To be documented

/**
 *
 * @param <E>
 */
public interface CacheAlgorithm<E> {

    /**
     *
     * @param elem
     * @return
     */
    E shift(E elem);

    /**
     *
     * @return
     */
    E delete();

    /**
     *
     * @param elem
     * @return
     */
    E delete(E elem);

    /**
     *
     */
    void flash();

    /**
     *
     * @return
     */
    String getType();
}
