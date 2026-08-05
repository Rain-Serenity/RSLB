package com.rserene.chosen.server.loader.classloader;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PriorAllURLClassLoader extends PriorURLClassLoader {
   private final Set<String> ignored;

   public PriorAllURLClassLoader(URL[] urls, ClassLoader parent, Set<String> ignored) {
      super(urls, parent, Collections.emptySet());
      this.ignored = new HashSet<>(ignored);
   }

   public PriorAllURLClassLoader(URL[] urls, ClassLoader parent) {
      this(urls, parent, new HashSet<>());
   }

   @Override
   public boolean containPrior(String name) {
      return !this.containIgnore(name);
   }

   @Override
   public Class<?> defineClass(String name, byte[] bytes) {
      return this.defineClass(name, bytes, 0, bytes.length);
   }

   private boolean containIgnore(String name) {
      for (String s : this.ignored) {
         if (name.startsWith(s)) {
            return true;
         }
      }

      return false;
   }

   static {
      registerAsParallelCapable();
   }
}
