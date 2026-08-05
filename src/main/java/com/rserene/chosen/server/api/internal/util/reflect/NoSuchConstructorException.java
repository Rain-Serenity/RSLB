package com.rserene.chosen.server.api.internal.util.reflect;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NoSuchConstructorException extends ReflectiveOperationException {
   public NoSuchConstructorException(String s) {
      super(s);
   }
}
