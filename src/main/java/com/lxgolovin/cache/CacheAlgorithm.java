package com.lxgolovin.cache;

public interface CacheAlgorithm<I> {
    I shift(I elem);
    I delete();
    I delete(I elem);
    void flash();
    String getType();
}
