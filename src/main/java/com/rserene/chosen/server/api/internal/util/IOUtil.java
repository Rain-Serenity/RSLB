package com.rserene.chosen.server.api.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class IOUtil {
   public static void copy(InputStream is, OutputStream os) throws IOException {
      byte[] buffer = new byte[1024];

      int n;
      while ((n = is.read(buffer)) != -1) {
         os.write(buffer, 0, n);
      }

      os.flush();
   }

   public static void removeAllFiles(File file) throws IOException {
      if (file.exists()) {
         if (!file.isFile()) {
            File[] files = file.listFiles();
            if (files == null) {
               return;
            }

            for (File f : files) {
               removeAllFiles(f);
            }
         }

         Files.delete(file.toPath());
      }
   }

   @Generated
   private IOUtil() {
   }
}
