package com.lxgolovin.cache.tools;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class FutureConvertor<T> {

    public static<T> List<T> listOfResults(List<CompletableFuture<T>> list) {
        return list.stream()
                .map(CompletableFuture::join)
                .collect(toList());
    }

    public static<T> CompletableFuture<Void> getAllFinished(List<CompletableFuture<T>> list) {
        return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0]));
    }

    public static<T> CompletableFuture<List<T>> listToFuture(List<CompletableFuture<T>> list) {
        return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> list.stream()
                        .map(CompletableFuture::join)
                        .collect(toList())
                );
    }
}
