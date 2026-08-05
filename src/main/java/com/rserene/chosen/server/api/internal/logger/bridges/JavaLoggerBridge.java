package com.rserene.chosen.server.api.internal.logger.bridges;

import java.util.logging.Logger;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class JavaLoggerBridge extends BaseLoggerBridge {
   private final Logger HANDLER;

   @Override
   public void log(Level level, String message, Throwable throwable) {
      if (level == Level.INFO) {
         this.HANDLER.log(java.util.logging.Level.INFO, message, throwable);
      } else if (level == Level.WARN) {
         this.HANDLER.log(java.util.logging.Level.WARNING, message, throwable);
      } else if (level == Level.ERROR) {
         this.HANDLER.log(java.util.logging.Level.SEVERE, message, throwable);
      }
   }

   @Generated
   public JavaLoggerBridge(Logger HANDLER) {
      this.HANDLER = HANDLER;
   }
}
