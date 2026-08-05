package com.rserene.chosen.server.api.internal.handle;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class HandleResult {
   private final HandleResult.Type type;
   private final String kickMessage;

   @Generated
   public HandleResult.Type getType() {
      return this.type;
   }

   @Generated
   public String getKickMessage() {
      return this.kickMessage;
   }

   @Generated
   public HandleResult(HandleResult.Type type, String kickMessage) {
      this.type = type;
      this.kickMessage = kickMessage;
   }

   public enum Type {
      NONE,
      KICK;
   }
}
