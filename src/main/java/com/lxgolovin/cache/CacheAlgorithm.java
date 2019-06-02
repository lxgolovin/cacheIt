package com.lxgolovin.cache;
// TODO: To be documented

/**
 *
 * @param <I>
 */
public interface CacheAlgorithm<I> {

    /**
     *
     * @param elem
     * @return
     */
    I shift(I elem);

    /**
     *
     * @return
     */
    I delete();

    /**
     *
     * @param elem
     * @return
     */
    I delete(I elem);

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
