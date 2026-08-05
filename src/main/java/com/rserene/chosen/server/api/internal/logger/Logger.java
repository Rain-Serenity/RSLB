package com.rserene.chosen.server.api.internal.logger;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Logger {
   void log(Level var1, String var2, Throwable var3);

   default void log(Level level, String message) {
      this.log(level, message, null);
   }

   default void log(Level level, Throwable throwable) {
      this.log(level, null, throwable);
   }

   default void debug(String message, Throwable throwable) {
      this.log(Level.DEBUG, message, throwable);
   }

   default void debug(String message) {
      this.log(Level.DEBUG, message);
   }

   default void debug(Throwable throwable) {
      this.log(Level.DEBUG, null, throwable);
   }

   default void info(String message, Throwable throwable) {
      this.log(Level.INFO, message, throwable);
   }

   default void info(String message) {
      this.log(Level.INFO, message);
   }

   default void info(Throwable throwable) {
      this.log(Level.INFO, null, throwable);
   }

   default void warn(String message, Throwable throwable) {
      this.log(Level.WARN, message, throwable);
   }

   default void warn(String message) {
      this.log(Level.WARN, message);
   }

   default void warn(Throwable throwable) {
      this.log(Level.WARN, null, throwable);
   }

   default void error(String message, Throwable throwable) {
      this.log(Level.ERROR, message, throwable);
   }

   default void error(String message) {
      this.log(Level.ERROR, message);
   }

   default void error(Throwable throwable) {
      this.log(Level.ERROR, null, throwable);
   }
}
