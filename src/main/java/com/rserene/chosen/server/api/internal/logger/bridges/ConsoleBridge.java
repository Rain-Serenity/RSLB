package com.rserene.chosen.server.api.internal.logger.bridges;

import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConsoleBridge extends BaseLoggerBridge {
   @Override
   public void log(Level level, String message, Throwable throwable) {
      if (level == Level.DEBUG) {
         System.out.println("[DEBUG] " + message);
         if (throwable != null) {
            throwable.printStackTrace(System.out);
         }
      } else if (level == Level.INFO) {
         System.out.println("[INFO] " + message);
         if (throwable != null) {
            throwable.printStackTrace(System.out);
         }
      } else if (level == Level.WARN) {
         System.out.println("[WARN] " + message);
         if (throwable != null) {
            throwable.printStackTrace(System.err);
         }
      } else if (level == Level.ERROR) {
         System.out.println("[ERROR] " + message);
         if (throwable != null) {
            throwable.printStackTrace(System.err);
         }
      }
   }
}
