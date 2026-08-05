package com.rserene.chosen.server.core.auth;

import lombok.Generated;
import com.rserene.chosen.server.api.internal.auth.AuthAPI;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.auth.service.BaseServiceAuthenticationResult;
import com.rserene.chosen.server.core.auth.service.yggdrasil.YggdrasilAuthenticationResult;
import com.rserene.chosen.server.core.auth.service.yggdrasil.YggdrasilAuthenticationService;
import com.rserene.chosen.server.core.auth.validate.ValidateAuthenticationResult;
import com.rserene.chosen.server.core.auth.validate.ValidateAuthenticationService;
import com.rserene.chosen.server.core.handle.PlayerHandler;
import com.rserene.chosen.server.core.main.RSLBCore;

public class AuthHandler implements AuthAPI {
   private final RSLBCore core;
   private final YggdrasilAuthenticationService yggdrasilAuthenticationService;
   private final ValidateAuthenticationService validateAuthenticationService;

   public AuthHandler(RSLBCore core) {
      this.core = core;
      this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
      this.validateAuthenticationService = new ValidateAuthenticationService(core);
   }

   public LoginAuthResult auth(String username, String serverId, String ip) {
      YggdrasilAuthenticationResult yggdrasilAuthenticationResult;
      try {
         yggdrasilAuthenticationResult = this.yggdrasilAuthenticationService.hasJoined(username, serverId, ip);
         if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.NO_SERVICE) {
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(
               yggdrasilAuthenticationResult, this.core.getLanguageHandler().getMessage("auth_failed_no_yggdrasil_service")
            );
         }

         if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(
               yggdrasilAuthenticationResult, this.core.getLanguageHandler().getMessage("auth_yggdrasil_failed_server_down")
            );
         }

         if (yggdrasilAuthenticationResult.getReason() == YggdrasilAuthenticationResult.Reason.VALIDATION_FAILED) {
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(
               yggdrasilAuthenticationResult, this.core.getLanguageHandler().getMessage("auth_yggdrasil_failed_validation_failed")
            );
         }

         if (yggdrasilAuthenticationResult.getReason() != YggdrasilAuthenticationResult.Reason.ALLOWED
            || yggdrasilAuthenticationResult.getResponse() == null
            || yggdrasilAuthenticationResult.getServiceConfig().getId() == -1) {
            return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(
               yggdrasilAuthenticationResult, this.core.getLanguageHandler().getMessage("auth_yggdrasil_failed_unknown")
            );
         }
      } catch (Exception e) {
         LoggerProvider.getLogger().error("An exception occurred while processing the hasJoined request.", e);
         return LoginAuthResult.ofDisallowedByYggdrasilAuthenticator(null, this.core.getLanguageHandler().getMessage("auth_yggdrasil_error"));
      }

      return this.checkIn(yggdrasilAuthenticationResult);
   }

   public LoginAuthResult checkIn(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
      try {
         ValidateAuthenticationResult validateAuthenticationResult = this.validateAuthenticationService.checkIn(baseServiceAuthenticationResult);
         if (validateAuthenticationResult.getReason() == ValidateAuthenticationResult.Reason.ALLOWED) {
            LoggerProvider.getLogger()
               .info(
                  String.format(
                     "%s(uuid: %s) from authentication service %s(sid: %d) has been authenticated, profile redirected to %s(uuid: %s).",
                     baseServiceAuthenticationResult.getResponse().getName(),
                     baseServiceAuthenticationResult.getResponse().getId().toString(),
                     baseServiceAuthenticationResult.getServiceConfig().getName(),
                     baseServiceAuthenticationResult.getServiceConfig().getId(),
                     validateAuthenticationResult.getInGameProfile().getName(),
                     validateAuthenticationResult.getInGameProfile().getId().toString()
                  )
               );
            GameProfile finalProfile = validateAuthenticationResult.getInGameProfile();
            this.core
               .getPlayerHandler()
               .getLoginCache()
               .put(
                  finalProfile.getId(),
                  new PlayerHandler.Entry(
                     baseServiceAuthenticationResult.getResponse(), baseServiceAuthenticationResult.getServiceConfig(), System.currentTimeMillis()
                  )
               );
            return LoginAuthResult.ofAllowed(baseServiceAuthenticationResult, validateAuthenticationResult, finalProfile);
         } else {
            return LoginAuthResult.ofDisallowedByValidateAuthenticator(
               baseServiceAuthenticationResult, validateAuthenticationResult, validateAuthenticationResult.getDisallowedMessage()
            );
         }
      } catch (Exception e) {
         LoggerProvider.getLogger().error("An exception occurred while processing the validation request.", e);
         return LoginAuthResult.ofDisallowedByValidateAuthenticator(
            baseServiceAuthenticationResult, null, this.core.getLanguageHandler().getMessage("auth_validate_error")
         );
      }
   }

   @Generated
   public RSLBCore getCore() {
      return this.core;
   }

   @Generated
   public YggdrasilAuthenticationService getYggdrasilAuthenticationService() {
      return this.yggdrasilAuthenticationService;
   }

   @Generated
   public ValidateAuthenticationService getValidateAuthenticationService() {
      return this.validateAuthenticationService;
   }
}
