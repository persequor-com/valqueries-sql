package com.valqueries.automapper;

public interface ThrowingFunction<T, X, E extends Throwable> {
    X apply(T t) throws E;
}
