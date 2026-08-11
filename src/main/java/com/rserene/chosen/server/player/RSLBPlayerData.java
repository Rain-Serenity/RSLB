package com.rserene.chosen.server.player;

import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface RSLBPlayerData {
   @NotNull
   GameProfile getOnlineProfile();

   @NotNull
   IService getLoginService();
}
