package com.rserene.chosen.server.api.internal.logger.bridges;

import com.rserene.chosen.server.api.internal.logger.Level;
import com.rserene.chosen.server.api.internal.logger.Logger;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DebugLoggerBridge implements Logger {
   private final Logger logger;

   public DebugLoggerBridge(Logger logger) {
      this.logger = logger;
   }

   public static void startDebugMode() {
      if (!(LoggerProvider.getLogger() instanceof DebugLoggerBridge)) {
         LoggerProvider.setLogger(new DebugLoggerBridge(LoggerProvider.getLogger()));
      }
   }

   public static void cancelDebugMode() {
      if (LoggerProvider.getLogger() instanceof DebugLoggerBridge) {
         LoggerProvider.setLogger(((DebugLoggerBridge)LoggerProvider.getLogger()).logger);
      }
   }

   @Override
   public void log(Level level, String message, Throwable throwable) {
      if (level == Level.DEBUG) {
         level = Level.INFO;
         message = "[DEBUG] " + message;
      }

      this.logger.log(level, message, throwable);
   }
}
