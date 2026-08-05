package com.rserene.chosen.server.core.configuration.service.yggdrasil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.configuration.ConfException;
import com.rserene.chosen.server.core.configuration.ProxyConfig;
import com.rserene.chosen.server.core.configuration.SkinRestorerConfig;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public abstract class BaseYggdrasilServiceConfig extends BaseServiceConfig {
   private final boolean trackIp;
   private final int timeout;
   private final int retry;
   private final long retryDelay;
   private final ProxyConfig authProxy;

   protected BaseYggdrasilServiceConfig(
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
      ProxyConfig authProxy
   ) throws ConfException {
      super(id, name, initUUID, initNameFormat, whitelist, skinRestorer);
      this.trackIp = trackIp;
      this.timeout = timeout;
      this.retry = retry;
      this.retryDelay = retryDelay;
      this.authProxy = authProxy;
   }

   public String generateAuthURL(String username, String serverId, String ip) {
      return ValueUtil.transPapi(
         this.getAuthURL(),
         new Pair[]{
            new Pair("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
            new Pair("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
            new Pair("ip", this.generateTraceIpContent(ip))
         }
      );
   }

   public String generateAuthPostContent(String username, String serverId, String ip) {
      return ValueUtil.transPapi(
         this.getAuthPostContent(),
         new Pair[]{
            new Pair("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
            new Pair("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
            new Pair("ip", this.generateTraceIpContent(ip))
         }
      );
   }

   private String generateTraceIpContent(String ip) {
      if (!this.trackIp) {
         return "";
      }

      if (ValueUtil.isEmpty(ip)) {
         return "";
      }

      String trackIpContent = this.getAuthTrackIpContent();
      return ValueUtil.isEmpty(trackIpContent) ? "" : ValueUtil.transPapi(trackIpContent, new Pair[]{new Pair("ip", ip)});
   }

   protected abstract String getAuthURL();

   protected abstract String getAuthPostContent();

   protected abstract String getAuthTrackIpContent();

   public abstract BaseYggdrasilServiceConfig.HttpRequestMethod getHttpRequestMethod();

   @Generated
   public boolean isTrackIp() {
      return this.trackIp;
   }

   @Generated
   public int getTimeout() {
      return this.timeout;
   }

   @Generated
   public int getRetry() {
      return this.retry;
   }

   @Generated
   public long getRetryDelay() {
      return this.retryDelay;
   }

   @Generated
   public ProxyConfig getAuthProxy() {
      return this.authProxy;
   }

   public enum HttpRequestMethod {
      GET,
      POST;
   }
}
