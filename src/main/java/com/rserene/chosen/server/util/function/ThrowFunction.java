package com.rserene.chosen.server.util.function;

import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Internal
public interface ThrowFunction<T, R> {
   R apply(T var1) throws Throwable;
}
