package com.rserene.chosen.server.api.internal.auth;

import com.rserene.chosen.server.api.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface AuthResult {
   GameProfile getResponse();

   String getKickMessage();

   AuthResult.Result getResult();

   enum Result {
      ALLOW,
      DISALLOW_BY_YGGDRASIL_AUTHENTICATOR,
      DISALLOW_BY_VALIDATE_AUTHENTICATOR,
      ERROR;
   }
}
