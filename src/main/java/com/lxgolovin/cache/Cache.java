package main.java.com.lxgolovin.cache;

public interface Cache<K, V> {
    V get(K key);
    K cache(K key, V value);
    K delete(K key);
    void clear();
    int size();
}
