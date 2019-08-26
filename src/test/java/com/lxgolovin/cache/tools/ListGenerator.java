package com.lxgolovin.cache.tools;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ListGenerator {

    private ListGenerator() {}

    public static List<Integer> generateInt(int size) {
        return IntStream
                .rangeClosed(1, size)
                .map(i -> (int)(Math.random() * size))
                .boxed()
                .collect(Collectors.toList());
    }
}
