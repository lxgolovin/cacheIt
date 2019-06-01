package com.lxgolovin.cache;

public interface CacheAlgorithm<I> {
    I shift(I elem);
    I delete();
    String getType();
}
