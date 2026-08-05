package com.rserene.chosen.server.api;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class RSLVAPIProvider {
   private static RSLVAPI api;

   @ApiStatus.Internal
   public static synchronized void setApi(RSLVAPI api) {
      if (RSLVAPIProvider.api != null) {
         throw new UnsupportedOperationException("duplicate api.");
      }

      RSLVAPIProvider.api = api;
   }

   @Generated
   public static RSLVAPI getApi() {
      return api;
   }
}
