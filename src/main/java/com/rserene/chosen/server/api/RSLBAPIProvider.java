package com.rserene.chosen.server.api;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class RSLBAPIProvider {
   private static RSLBAPI api;

   @ApiStatus.Internal
   public static synchronized void setApi(RSLBAPI api) {
      if (RSLBAPIProvider.api != null) {
         throw new UnsupportedOperationException("duplicate api.");
      }

      RSLBAPIProvider.api = api;
   }

   @Generated
   public static RSLBAPI getApi() {
      return api;
   }
}
