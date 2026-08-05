package com.rserene.chosen.server.api.data;

import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface RSLBPlayerData {
   @NotNull
   GameProfile getOnlineProfile();

   @NotNull
   IService getLoginService();
}
