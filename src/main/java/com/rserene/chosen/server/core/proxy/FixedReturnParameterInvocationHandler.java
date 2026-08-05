package com.rserene.chosen.server.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import com.rserene.chosen.server.api.internal.function.BiConsumerFunction;

public class FixedReturnParameterInvocationHandler implements InvocationHandler {
   private final Object handle;
   private final Function<Method, Boolean> match;
   private final BiConsumerFunction<Object, Object, Object> fixedFunc;

   public FixedReturnParameterInvocationHandler(Object handle, Function<Method, Boolean> match, BiConsumerFunction<Object, Object, Object> fixedFunc) {
      this.handle = handle;
      this.match = match;
      this.fixedFunc = fixedFunc;

      for (Method method : handle.getClass().getDeclaredMethods()) {
         if (!Modifier.isStatic(method.getModifiers()) && match.apply(method)) {
            return;
         }
      }

      for (Method method : handle.getClass().getMethods()) {
         if (!Modifier.isStatic(method.getModifiers()) && match.apply(method)) {
            return;
         }
      }

      throw new RuntimeException("Methods may never be matched.");
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
      return this.match.apply(method) ? this.fixedFunc.accept(this.handle, args) : method.invoke(this.handle, args);
   }
}
