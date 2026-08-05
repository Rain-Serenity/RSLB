package com.rserene.chosen.server.core.configuration.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiFunction;
import lombok.Generated;
import com.rserene.chosen.server.api.service.IService;
import com.rserene.chosen.server.api.service.ServiceType;
import com.rserene.chosen.server.core.configuration.ConfException;
import com.rserene.chosen.server.core.configuration.SkinRestorerConfig;
import org.jetbrains.annotations.NotNull;

public abstract class BaseServiceConfig implements IService {
   private final int id;
   private final String name;
   private final BaseServiceConfig.InitUUID initUUID;
   private final String initNameFormat;
   private final boolean whitelist;
   private final SkinRestorerConfig skinRestorer;

   protected BaseServiceConfig(
      int id, String name, BaseServiceConfig.InitUUID initUUID, String initNameFormat, boolean whitelist, SkinRestorerConfig skinRestorer
   ) throws ConfException {
      this.id = id;
      this.name = name;
      this.initUUID = initUUID;
      this.initNameFormat = initNameFormat;
      this.whitelist = whitelist;
      this.skinRestorer = skinRestorer;
      this.checkValid();
   }

   protected void checkValid() throws ConfException {
      if (this.id > 127 || this.id < 0) {
         throw new ConfException(String.format("Yggdrasil id %d is out of bounds, The value can only be between 0 and 127.", this.id));
      }
   }

   public String generateName(String loginName) {
      return this.initNameFormat.replace("{name}", loginName).replace(" ", "_");
   }

   public int getServiceId() {
      return this.id;
   }

   @NotNull
   public String getServiceName() {
      return this.name;
   }

   @NotNull
   public abstract ServiceType getServiceType();

   @Generated
   public int getId() {
      return this.id;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public BaseServiceConfig.InitUUID getInitUUID() {
      return this.initUUID;
   }

   @Generated
   public String getInitNameFormat() {
      return this.initNameFormat;
   }

   @Generated
   public boolean isWhitelist() {
      return this.whitelist;
   }

   @Generated
   public SkinRestorerConfig getSkinRestorer() {
      return this.skinRestorer;
   }

   public enum InitUUID {
      DEFAULT((u, n) -> u),
      OFFLINE((u, n) -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + n).getBytes(StandardCharsets.UTF_8))),
      RANDOM((u, n) -> UUID.randomUUID());

      private final BiFunction<UUID, String, UUID> biFunction;

      InitUUID(BiFunction<UUID, String, UUID> biFunction) {
         this.biFunction = biFunction;
      }

      public UUID generateUUID(UUID onlineUUID, String currentUsername) {
         return this.biFunction.apply(onlineUUID, currentUsername);
      }
   }
}
