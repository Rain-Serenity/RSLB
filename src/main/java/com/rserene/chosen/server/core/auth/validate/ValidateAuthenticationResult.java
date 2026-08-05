package com.rserene.chosen.server.core.auth.validate;

import lombok.Generated;
import com.rserene.chosen.server.api.profile.GameProfile;

public class ValidateAuthenticationResult {
   private final ValidateAuthenticationResult.Reason reason;
   private final GameProfile inGameProfile;
   private final String disallowedMessage;

   public static ValidateAuthenticationResult ofAllowed(GameProfile response) {
      return new ValidateAuthenticationResult(ValidateAuthenticationResult.Reason.ALLOWED, response, null);
   }

   public static ValidateAuthenticationResult ofDisallowed(String disallowedMessage) {
      return new ValidateAuthenticationResult(ValidateAuthenticationResult.Reason.DISALLOWED, null, disallowedMessage);
   }

   @Generated
   public ValidateAuthenticationResult.Reason getReason() {
      return this.reason;
   }

   @Generated
   public GameProfile getInGameProfile() {
      return this.inGameProfile;
   }

   @Generated
   public String getDisallowedMessage() {
      return this.disallowedMessage;
   }

   @Generated
   private ValidateAuthenticationResult(ValidateAuthenticationResult.Reason reason, GameProfile inGameProfile, String disallowedMessage) {
      this.reason = reason;
      this.inGameProfile = inGameProfile;
      this.disallowedMessage = disallowedMessage;
   }

   @Generated
   @Override
   public String toString() {
      return "ValidateAuthenticationResult(reason="
         + this.getReason()
         + ", inGameProfile="
         + this.getInGameProfile()
         + ", disallowedMessage="
         + this.getDisallowedMessage()
         + ")";
   }

   public enum Reason {
      ALLOWED,
      DISALLOWED;
   }
}
