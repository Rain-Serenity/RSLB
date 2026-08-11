package com.rserene.chosen.server.player;

import java.util.UUID;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.profile.GameProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface HandlerAPI {
   HandleResult pushPlayerQuitGame(UUID var1, String var2);

   HandleResult pushPlayerJoinGame(UUID var1, String var2);

   void callPlayerJoinGame(Player var1);

   Pair<GameProfile, Integer> getPlayerOnlineProfile(UUID var1);

   UUID getInGameUUID(UUID var1, int var2);

   String getServiceName(int var1);
}
