package com.lxgolovin.cache;

public interface CacheAlgorithm<I> {
    I shift(I elem);
    I unshift(I elem);
    I delete();
    void flash();
    String getType();
}
