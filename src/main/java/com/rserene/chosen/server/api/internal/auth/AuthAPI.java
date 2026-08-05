package com.rserene.chosen.server.api.internal.auth;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface AuthAPI {
   AuthResult auth(String var1, String var2, String var3);
}
