package com.rserene.chosen.server.api.internal.function;

import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Internal
public interface BiConsumerFunction<T, U, R> {
   R accept(T var1, U var2);
}
