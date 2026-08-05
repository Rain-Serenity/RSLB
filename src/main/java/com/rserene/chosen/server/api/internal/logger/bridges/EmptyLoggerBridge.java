package com.rserene.chosen.server.api.internal.logger.bridges;

import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EmptyLoggerBridge extends BaseLoggerBridge {
   @Override
   public void log(Level level, String message, Throwable throwable) {
   }
}
