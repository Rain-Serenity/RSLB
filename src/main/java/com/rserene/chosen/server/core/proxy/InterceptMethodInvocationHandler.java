package com.rserene.chosen.server.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InterceptMethodInvocationHandler implements InvocationHandler {
   private final Object handle;
   private final Function<Method, Boolean> match;
   private final BiFunction<Method, Object[], Object> redirect;

   public InterceptMethodInvocationHandler(Object handle, Function<Method, Boolean> match, BiFunction<Method, Object[], Object> redirect) {
      this.handle = handle;
      this.match = match;
      this.redirect = redirect;

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
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return this.match.apply(method) ? this.redirect.apply(method, args) : method.invoke(this.handle, args);
   }
}
