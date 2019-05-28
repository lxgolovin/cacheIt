package main.java.com.lxgolovin.cacheit;

public interface Cache<K, V> {

    V getValue(K key);
    K cacheMe(K key, V value);
    K deleteValue(K key);
    void clearAll();
    Integer cacheSize();
}
