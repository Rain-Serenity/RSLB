package com.rserene.chosen.server.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public class OtherAppClassLoader extends URLClassLoader implements IExtURLClassLoader {
   public static final ClassLoader extClassLoader = ClassLoader.getSystemClassLoader().getParent();

   public OtherAppClassLoader(URL[] urls) {
      super(urls, extClassLoader);
   }

   @Override
   public void addURL(URL url) {
      super.addURL(url);
   }

   @Override
   public URLClassLoader self() {
      return this;
   }

   @Override
   public Class<?> defineClass(String name, byte[] bytes) {
      return this.defineClass(name, bytes, 0, bytes.length);
   }

   static {
      registerAsParallelCapable();
   }
}
