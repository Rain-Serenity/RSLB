package com.rserene.chosen.server.core.auth.service;

import lombok.Generated;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public abstract class BaseServiceAuthenticationResult {
   private final GameProfile response;
   private final BaseServiceConfig serviceConfig;

   public BaseServiceAuthenticationResult(GameProfile response, BaseServiceConfig serviceConfig) {
      this.response = response;
      this.serviceConfig = serviceConfig;
   }

   public abstract boolean isAllowed();

   @Generated
   public GameProfile getResponse() {
      return this.response;
   }

   @Generated
   public BaseServiceConfig getServiceConfig() {
      return this.serviceConfig;
   }
}
