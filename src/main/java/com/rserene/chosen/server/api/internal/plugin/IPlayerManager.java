package com.rserene.chosen.server.api.internal.plugin;

import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IPlayerManager {
   Set<IPlayer> getPlayers(String var1);

   IPlayer getPlayer(UUID var1);

   Set<IPlayer> getOnlinePlayers();

   default void kickPlayerIfOnline(String name, String message) {
      for (IPlayer player : this.getPlayers(name)) {
         player.kickPlayer(message);
      }
   }

   default void kickAll(String message) {
      for (IPlayer player : this.getOnlinePlayers()) {
         player.kickPlayer(message);
      }
   }

   default void kickPlayerIfOnline(UUID uuid, String message) {
      IPlayer player = this.getPlayer(uuid);
      if (player != null) {
         player.kickPlayer(message);
      }
   }

   default boolean hasOnline(UUID redirectUuid) {
      return this.getPlayer(redirectUuid) != null;
   }
}
