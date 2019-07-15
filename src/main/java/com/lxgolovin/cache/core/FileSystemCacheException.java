package com.lxgolovin.cache.core;

public class FileSystemCacheException extends RuntimeException {

    public FileSystemCacheException(String message){
        super(message);
    }

    public FileSystemCacheException(String s, Throwable throwable) {
        super(s, throwable);
    }
}