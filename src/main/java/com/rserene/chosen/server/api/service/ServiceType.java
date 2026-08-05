package com.rserene.chosen.server.api.service;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public enum ServiceType {
   OFFICIAL(true),
   LITTLESKIN(true);

   private final boolean yggdrasilService;

   ServiceType(boolean yggdrasilService) {
      this.yggdrasilService = yggdrasilService;
   }

   public boolean isYggdrasilService() {
      return this.yggdrasilService;
   }
}
