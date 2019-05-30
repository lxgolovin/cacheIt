package main.java.com.lxgolovin.cache;

public interface CacheAlgo<K, V> {
    K head();
    K tail();
    K renew(K key, V value);
    K add(K key, V value);
    K del(K key, V value);
}
