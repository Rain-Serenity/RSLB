package com.rserene.chosen.server.loader.classloader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class RelocateClassLoader extends URLClassLoader implements IExtURLClassLoader {
   private final Set<String> relocates;
   private final String appendPrefix;

   public RelocateClassLoader(URL[] urls, Set<String> relocates, String appendPrefix, ClassLoader parent) {
      super(urls, parent);
      this.appendPrefix = appendPrefix;
      this.relocates = Collections.unmodifiableSet(relocates);
   }

   @Override
   protected Class<?> findClass(String name) throws ClassNotFoundException {
      if (name.startsWith(this.appendPrefix)) {
         try {
            String vanillaName = name.substring(this.appendPrefix.length());
            String path = vanillaName.replace('.', '/').concat(".class");
            InputStream inputStream = this.getResourceAsStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int code;
            while ((code = inputStream.read()) != -1) {
               baos.write(code);
            }

            byte[] bytes = baos.toByteArray();
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(0);
            cr.accept(new ClassRemapper(cw, new RelocateClassLoader.AppendPrefixMapper()), 8);
            bytes = cw.toByteArray();
            return this.defineClass(name, bytes, 0, bytes.length);
         } catch (Exception var10) {
         }
      }

      return super.findClass(name);
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

   private class AppendPrefixMapper extends Remapper {
      public String map(String internalName) {
         for (String relocate : RelocateClassLoader.this.relocates) {
            if (internalName.startsWith(relocate.replace('.', '/'))) {
               return RelocateClassLoader.this.appendPrefix.replace('.', '/') + internalName;
            }
         }

         return super.map(internalName);
      }
   }
}
