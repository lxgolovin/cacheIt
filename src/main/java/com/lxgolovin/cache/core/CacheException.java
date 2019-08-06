package com.lxgolovin.cache.core;

public class CacheException extends RuntimeException {

    public CacheException(String message){
        super(message);
    }

    public CacheException(String s, Throwable throwable) {
        super(s, throwable);
    }
}