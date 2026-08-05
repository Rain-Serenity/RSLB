package com.rserene.chosen.server.core.auth;

import lombok.Generated;
import com.rserene.chosen.server.api.internal.auth.AuthResult;
import com.rserene.chosen.server.api.internal.auth.AuthResult.Result;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.auth.service.BaseServiceAuthenticationResult;
import com.rserene.chosen.server.core.auth.service.yggdrasil.UnmodifiableGameProfile;
import com.rserene.chosen.server.core.auth.service.yggdrasil.YggdrasilAuthenticationResult;
import com.rserene.chosen.server.core.auth.validate.ValidateAuthenticationResult;

public class LoginAuthResult implements AuthResult {
   private final UnmodifiableGameProfile response;
   private final String kickMessage;
   private final Result result;
   private final BaseServiceAuthenticationResult baseServiceAuthenticationResult;
   private final ValidateAuthenticationResult validateAuthenticationResult;

   protected LoginAuthResult(
      UnmodifiableGameProfile response,
      String kickMessage,
      Result result,
      BaseServiceAuthenticationResult baseServiceAuthenticationResult,
      ValidateAuthenticationResult validateAuthenticationResult
   ) {
      this.response = response;
      this.kickMessage = kickMessage;
      this.result = result;
      this.baseServiceAuthenticationResult = baseServiceAuthenticationResult;
      this.validateAuthenticationResult = validateAuthenticationResult;
   }

   public static LoginAuthResult ofDisallowedByYggdrasilAuthenticator(YggdrasilAuthenticationResult yggdrasilAuthenticationResult, String kickMessage) {
      return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_YGGDRASIL_AUTHENTICATOR, yggdrasilAuthenticationResult, null);
   }

   public static LoginAuthResult ofDisallowedByValidateAuthenticator(
      BaseServiceAuthenticationResult baseServiceAuthenticationResult, ValidateAuthenticationResult validateAuthenticationResult, String kickMessage
   ) {
      return new LoginAuthResult(null, kickMessage, Result.DISALLOW_BY_VALIDATE_AUTHENTICATOR, baseServiceAuthenticationResult, validateAuthenticationResult);
   }

   public static LoginAuthResult ofAllowed(
      BaseServiceAuthenticationResult baseServiceAuthenticationResult, ValidateAuthenticationResult validateAuthenticationResult, GameProfile gameProfile
   ) {
      return new LoginAuthResult(
         UnmodifiableGameProfile.unmodifiable(gameProfile), null, Result.ALLOW, baseServiceAuthenticationResult, validateAuthenticationResult
      );
   }

   @Generated
   public UnmodifiableGameProfile getResponse() {
      return this.response;
   }

   @Generated
   public String getKickMessage() {
      return this.kickMessage;
   }

   @Generated
   public Result getResult() {
      return this.result;
   }

   @Generated
   public BaseServiceAuthenticationResult getBaseServiceAuthenticationResult() {
      return this.baseServiceAuthenticationResult;
   }

   @Generated
   public ValidateAuthenticationResult getValidateAuthenticationResult() {
      return this.validateAuthenticationResult;
   }
}
