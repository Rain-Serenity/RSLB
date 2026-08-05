package com.rserene.chosen.server.api.internal.handle;

import java.util.UUID;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface HandlerAPI {
   HandleResult pushPlayerQuitGame(UUID var1, String var2);

   HandleResult pushPlayerJoinGame(UUID var1, String var2);

   void callPlayerJoinGame(IPlayer var1);

   Pair<GameProfile, Integer> getPlayerOnlineProfile(UUID var1);

   UUID getInGameUUID(UUID var1, int var2);

   String getServiceName(int var1);
}
