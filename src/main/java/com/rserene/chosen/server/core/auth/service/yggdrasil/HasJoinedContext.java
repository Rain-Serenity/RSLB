package com.rserene.chosen.server.core.auth.service.yggdrasil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;

public class HasJoinedContext {
   private final String username;
   private final String serverId;
   private final String ip;
   private final AtomicReference<Pair<GameProfile, BaseYggdrasilServiceConfig>> response = new AtomicReference<>();
   private final Map<BaseYggdrasilServiceConfig, Throwable> serviceUnavailable = new ConcurrentHashMap<>();
   private final Set<Integer> authenticationFailed = ConcurrentHashMap.newKeySet();

   protected HasJoinedContext(String username, String serverId, String ip) {
      this.username = username;
      this.serverId = serverId;
      this.ip = ip;
   }

   @Generated
   public String getUsername() {
      return this.username;
   }

   @Generated
   public String getServerId() {
      return this.serverId;
   }

   @Generated
   public String getIp() {
      return this.ip;
   }

   @Generated
   public AtomicReference<Pair<GameProfile, BaseYggdrasilServiceConfig>> getResponse() {
      return this.response;
   }

   @Generated
   public Map<BaseYggdrasilServiceConfig, Throwable> getServiceUnavailable() {
      return this.serviceUnavailable;
   }

   @Generated
   public Set<Integer> getAuthenticationFailed() {
      return this.authenticationFailed;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof HasJoinedContext other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         Object this$username = this.getUsername();
         Object other$username = other.getUsername();
         if (this$username == null ? other$username == null : this$username.equals(other$username)) {
            Object this$serverId = this.getServerId();
            Object other$serverId = other.getServerId();
            if (this$serverId == null ? other$serverId == null : this$serverId.equals(other$serverId)) {
               Object this$ip = this.getIp();
               Object other$ip = other.getIp();
               if (this$ip == null ? other$ip == null : this$ip.equals(other$ip)) {
                  Object this$response = this.getResponse();
                  Object other$response = other.getResponse();
                  if (this$response == null ? other$response == null : this$response.equals(other$response)) {
                     Object this$serviceUnavailable = this.getServiceUnavailable();
                     Object other$serviceUnavailable = other.getServiceUnavailable();
                     if (this$serviceUnavailable == null ? other$serviceUnavailable == null : this$serviceUnavailable.equals(other$serviceUnavailable)) {
                        Object this$authenticationFailed = this.getAuthenticationFailed();
                        Object other$authenticationFailed = other.getAuthenticationFailed();
                        return this$authenticationFailed == null
                           ? other$authenticationFailed == null
                           : this$authenticationFailed.equals(other$authenticationFailed);
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof HasJoinedContext;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $username = this.getUsername();
      result = result * 59 + ($username == null ? 43 : $username.hashCode());
      Object $serverId = this.getServerId();
      result = result * 59 + ($serverId == null ? 43 : $serverId.hashCode());
      Object $ip = this.getIp();
      result = result * 59 + ($ip == null ? 43 : $ip.hashCode());
      Object $response = this.getResponse();
      result = result * 59 + ($response == null ? 43 : $response.hashCode());
      Object $serviceUnavailable = this.getServiceUnavailable();
      result = result * 59 + ($serviceUnavailable == null ? 43 : $serviceUnavailable.hashCode());
      Object $authenticationFailed = this.getAuthenticationFailed();
      return result * 59 + ($authenticationFailed == null ? 43 : $authenticationFailed.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "HasJoinedContext(username="
         + this.getUsername()
         + ", serverId="
         + this.getServerId()
         + ", ip="
         + this.getIp()
         + ", response="
         + this.getResponse()
         + ", serviceUnavailable="
         + this.getServiceUnavailable()
         + ", authenticationFailed="
         + this.getAuthenticationFailed()
         + ")";
   }
}
