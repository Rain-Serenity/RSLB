package com.rserene.chosen.server.api.internal.skinrestorer;

import com.rserene.chosen.server.api.internal.auth.AuthResult;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerAPI {
   SkinRestorerResult doRestorer(AuthResult var1);
}
