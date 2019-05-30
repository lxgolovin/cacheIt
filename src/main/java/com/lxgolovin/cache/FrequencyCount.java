package com.lxgolovin.cache;

public interface FrequencyCount<K> {
    int frequency(K key);
    K mostUsed();
    K leastUsed();
}
