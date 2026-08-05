package com.rserene.chosen.server.api.internal.logger;

import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.bridges.ConsoleBridge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LoggerProvider {
   private static Logger logger = new ConsoleBridge();

   @Generated
   public static Logger getLogger() {
      return logger;
   }

   @Generated
   public static void setLogger(Logger logger) {
      LoggerProvider.logger = logger;
   }
}
