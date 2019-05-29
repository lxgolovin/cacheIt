package main.java.com.lxgolovin.cache;

public interface Cache<K, V> {
    V get(K key);
    K cache(K key, V value);
    K del(K key);
    void clear();
    int size();
}
