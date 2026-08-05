package com.rserene.chosen.server.core.auth.service.yggdrasil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;
import com.rserene.chosen.server.core.main.RSLBCore;
import com.rserene.chosen.server.core.ohc.LoggingInterceptor;
import com.rserene.chosen.server.core.ohc.RetryInterceptor;
import com.rserene.chosen.server.flows.workflows.BaseFlows;
import com.rserene.chosen.server.flows.workflows.Signal;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class YggdrasilAuthenticationFlows extends BaseFlows<HasJoinedContext> {
   private final RSLBCore core;
   private final String username;
   private final String serverId;
   private final String ip;
   private final BaseYggdrasilServiceConfig config;

   protected YggdrasilAuthenticationFlows(RSLBCore core, String username, String serverId, String ip, BaseYggdrasilServiceConfig config) {
      this.core = core;
      this.username = username;
      this.serverId = serverId;
      this.ip = ip;
      this.config = config;
   }

   public GameProfile call() throws Exception {
      String url = this.config.generateAuthURL(this.username, this.serverId, this.ip);
      if (this.config.getHttpRequestMethod() == BaseYggdrasilServiceConfig.HttpRequestMethod.GET) {
         return this.call0(this.config, new Builder().get().url(url).header("User-Agent", this.core.getHttpRequestHeaderUserAgent()).build());
      } else if (this.config.getHttpRequestMethod() == BaseYggdrasilServiceConfig.HttpRequestMethod.POST) {
         return this.call0(
            this.config,
            new Builder()
               .post(RequestBody.create(this.config.generateAuthPostContent(this.username, this.serverId, this.ip).getBytes(StandardCharsets.UTF_8)))
               .url(url)
               .header("User-Agent", this.core.getHttpRequestHeaderUserAgent())
               .header("Content-Type", "application/json")
               .build()
         );
      } else {
         throw new UnsupportedOperationException("HttpRequestMethod");
      }
   }

   private GameProfile call0(BaseYggdrasilServiceConfig config, Request request) throws IOException {
      OkHttpClient client = new okhttp3.OkHttpClient.Builder()
         .addInterceptor(new RetryInterceptor(config.getRetry(), config.getRetryDelay()))
         .addInterceptor(new LoggingInterceptor())
         .writeTimeout(Duration.ofMillis(config.getTimeout()))
         .readTimeout(Duration.ofMillis(config.getTimeout()))
         .connectTimeout(Duration.ofMillis(config.getTimeout()))
         .proxy(config.getAuthProxy().getProxy())
         .proxyAuthenticator(config.getAuthProxy().getProxyAuthenticator())
         .build();
      Call call = client.newCall(request);
      Response execute = call.execute();

      GameProfile var6;
      try {
         var6 = (GameProfile)this.core.getGson().fromJson(Objects.requireNonNull(execute.body()).string(), GameProfile.class);
      } catch (Throwable var9) {
         if (execute != null) {
            try {
               execute.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (execute != null) {
         execute.close();
      }

      return var6;
   }

   public Signal run(HasJoinedContext hasJoinedContext) {
      try {
         GameProfile call = this.call();
         if (call != null && call.getId() != null) {
            hasJoinedContext.getResponse().set(new Pair(call, this.config));
            return Signal.PASSED;
         } else {
            return Signal.TERMINATED;
         }
      } catch (Throwable e) {
         hasJoinedContext.getServiceUnavailable().put(this.config, e);
         return Signal.TERMINATED;
      }
   }
}
