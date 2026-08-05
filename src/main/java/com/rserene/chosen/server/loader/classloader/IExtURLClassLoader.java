package com.rserene.chosen.server.loader.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public interface IExtURLClassLoader {
   void addURL(URL var1);

   URLClassLoader self();

   Class<?> defineClass(String var1, byte[] var2);
}
