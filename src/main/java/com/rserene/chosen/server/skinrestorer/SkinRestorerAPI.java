package com.rserene.chosen.server.skinrestorer;

import com.rserene.chosen.server.auth.AuthResult;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerAPI {
   SkinRestorerResult doRestorer(AuthResult var1);
}
