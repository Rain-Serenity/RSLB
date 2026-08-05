package com.rserene.chosen.server.core.auth.service.yggdrasil;

import lombok.Generated;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.auth.service.BaseServiceAuthenticationResult;
import com.rserene.chosen.server.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;

public class YggdrasilAuthenticationResult extends BaseServiceAuthenticationResult {
   private final YggdrasilAuthenticationResult.Reason reason;

   public YggdrasilAuthenticationResult(YggdrasilAuthenticationResult.Reason reason, GameProfile response, BaseYggdrasilServiceConfig serviceConfig) {
      super(response, serviceConfig);
      this.reason = reason;
   }

   protected static YggdrasilAuthenticationResult ofAllowed(GameProfile response, BaseYggdrasilServiceConfig serviceConfig) {
      return new YggdrasilAuthenticationResult(YggdrasilAuthenticationResult.Reason.ALLOWED, response, serviceConfig);
   }

   protected static YggdrasilAuthenticationResult ofServerBreakdown() {
      return new YggdrasilAuthenticationResult(YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN, null, null);
   }

   protected static YggdrasilAuthenticationResult ofValidationFailed() {
      return new YggdrasilAuthenticationResult(YggdrasilAuthenticationResult.Reason.VALIDATION_FAILED, null, null);
   }

   protected static YggdrasilAuthenticationResult ofNoService() {
      return new YggdrasilAuthenticationResult(YggdrasilAuthenticationResult.Reason.NO_SERVICE, null, null);
   }

   @Override
   public boolean isAllowed() {
      return this.reason == YggdrasilAuthenticationResult.Reason.ALLOWED;
   }

   @Generated
   public YggdrasilAuthenticationResult.Reason getReason() {
      return this.reason;
   }

   @Generated
   @Override
   public String toString() {
      return "YggdrasilAuthenticationResult(reason=" + this.getReason() + ")";
   }

   public enum Reason {
      ALLOWED,
      SERVER_BREAKDOWN,
      VALIDATION_FAILED,
      NO_SERVICE;
   }
}
