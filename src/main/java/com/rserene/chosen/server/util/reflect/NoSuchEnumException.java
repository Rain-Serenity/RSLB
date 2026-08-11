package com.rserene.chosen.server.util.reflect;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NoSuchEnumException extends ReflectiveOperationException {
   public NoSuchEnumException(String s) {
      super(s);
   }
}
