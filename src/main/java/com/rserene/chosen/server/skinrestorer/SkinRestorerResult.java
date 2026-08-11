package com.rserene.chosen.server.skinrestorer;

import com.rserene.chosen.server.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerResult {
   SkinRestorerResult.Reason getReason();

   GameProfile getResponse();

   Throwable getThrowable();

   enum Reason {
      NO_SKIN,
      NO_RESTORER,
      USE_CACHE,
      SIGNATURE_VALID,
      BAD_SKIN,
      RESTORER_SUCCEED,
      RESTORER_FAILED;
   }
}
