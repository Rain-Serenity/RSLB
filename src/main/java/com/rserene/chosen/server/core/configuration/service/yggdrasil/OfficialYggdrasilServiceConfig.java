package com.rserene.chosen.server.core.configuration.service.yggdrasil;

import com.rserene.chosen.server.api.service.ServiceType;
import com.rserene.chosen.server.core.configuration.ConfException;
import com.rserene.chosen.server.core.configuration.ProxyConfig;
import com.rserene.chosen.server.core.configuration.SkinRestorerConfig;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import org.jetbrains.annotations.NotNull;

public class OfficialYggdrasilServiceConfig extends BaseYggdrasilServiceConfig {
   private final String customSessionServer;

   public OfficialYggdrasilServiceConfig(
      int id,
      String name,
      BaseServiceConfig.InitUUID initUUID,
      String initNameFormat,
      boolean whitelist,
      SkinRestorerConfig skinRestorer,
      boolean trackIp,
      int timeout,
      int retry,
      long retryDelay,
      ProxyConfig authProxy,
      String customSessionServer
   ) throws ConfException {
      super(id, name, initUUID, initNameFormat, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy);
      if (!customSessionServer.endsWith("/")) {
         customSessionServer = customSessionServer.concat("/");
      }

      this.customSessionServer = customSessionServer;
   }

   @Override
   protected String getAuthURL() {
      String baseUrl = this.customSessionServer;
      return baseUrl.concat("session/minecraft/hasJoined?username={0}&serverId={1}{2}");
   }

   @Override
   protected String getAuthPostContent() {
      throw new UnsupportedOperationException("get post content");
   }

   @Override
   protected String getAuthTrackIpContent() {
      return "&ip={0}";
   }

   @Override
   public BaseYggdrasilServiceConfig.HttpRequestMethod getHttpRequestMethod() {
      return BaseYggdrasilServiceConfig.HttpRequestMethod.GET;
   }

   @NotNull
   @Override
   public ServiceType getServiceType() {
      return ServiceType.OFFICIAL;
   }
}
