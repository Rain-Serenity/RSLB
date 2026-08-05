package com.rserene.chosen.server.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class PriorURLClassLoader extends URLClassLoader implements IExtURLClassLoader {
   private final Set<String> packageName;

   public PriorURLClassLoader(URL[] urls, ClassLoader parent, Set<String> packageName) {
      super(urls, parent);
      this.packageName = new HashSet<>(packageName);
   }

   @Override
   public Class<?> defineClass(String name, byte[] bytes) {
      return this.defineClass(name, bytes, 0, bytes.length);
   }

   @Override
   protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      synchronized (this.getClassLoadingLock(name)) {
         Class<?> c = this.findLoadedClass(name);
         if (c == null && this.containPrior(name)) {
            Class var10000;
            try {
               c = this.findClass(name);
               if (resolve) {
                  this.resolveClass(c);
               }

               var10000 = c;
            } catch (ClassNotFoundException var7) {
               return super.loadClass(name, resolve);
            }

            return var10000;
         }
      }

      return super.loadClass(name, resolve);
   }

   @Override
   public void addURL(URL url) {
      super.addURL(url);
   }

   @Override
   public URLClassLoader self() {
      return this;
   }

   public boolean containPrior(String name) {
      for (String s : this.packageName) {
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
