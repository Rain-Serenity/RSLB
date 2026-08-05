package com.rserene.chosen.server.core.handle;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Generated;
import com.rserene.chosen.server.api.data.RSLBPlayerData;
import com.rserene.chosen.server.api.internal.handle.HandleResult;
import com.rserene.chosen.server.api.internal.handle.HandlerAPI;
import com.rserene.chosen.server.api.internal.handle.HandleResult.Type;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.service.IService;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.main.RSLBCore;
import org.jetbrains.annotations.NotNull;

public class PlayerHandler implements HandlerAPI {
   private final RSLBCore core;
   private final Map<UUID, PlayerHandler.Entry> cache;
   private final Map<UUID, PlayerHandler.Entry> loginCache;

   public PlayerHandler(RSLBCore core) {
      this.core = core;
      this.cache = new ConcurrentHashMap<>();
      this.loginCache = new ConcurrentHashMap<>();
   }

   public HandleResult pushPlayerQuitGame(UUID inGameUUID, String username) {
      return new HandleResult(Type.NONE, null);
   }

   public HandleResult pushPlayerJoinGame(UUID inGameUUID, String username) {
      PlayerHandler.Entry remove = this.loginCache.remove(inGameUUID);
      if (remove == null) {
         if (this.core.getPluginConfig().isForceUseLogin()) {
            return new HandleResult(Type.KICK, this.core.getLanguageHandler().getMessage("auth_handler_need_use_login"));
         }

         LoggerProvider.getLogger()
            .warn(
               String.format(
                  "The player with in game UUID %s and name %s is not logged into the server by RSLB, some features will be disabled for him.",
                  inGameUUID.toString(),
                  username
               )
            );
      } else {
         long l = System.currentTimeMillis() - remove.signTimeMillis;
         if (l > 5000L) {
            LoggerProvider.getLogger()
               .warn(
                  String.format(
                     "Players with in game UUID %s and name %s are taking too long to log in after verification, reached %d milliseconds. Is it the same person?",
                     inGameUUID.toString(),
                     username,
                     l
                  )
               );
         }

         this.cache.put(inGameUUID, remove);
      }

      return new HandleResult(Type.NONE, null);
   }

   public void callPlayerJoinGame(IPlayer player) {
      if (this.core.getPluginConfig().isWelcomeMsg()) {
         this.core
            .getPlugin()
            .getRunServer()
            .getScheduler()
            .runTaskAsync(
               () -> {
                  Pair<GameProfile, BaseServiceConfig> pair = this.getPlayerOnlineProfile0(player.getUniqueId());
                  String msg;
                  if (pair == null) {
                      msg = this.core
                         .getLanguageHandler()
                         .getMessage("welcome_msg_to_unknown", new Pair("profile_name", player.getName()), new Pair("profile_uuid", player.getUniqueId()));
                  } else {
                     msg = this.core
                        .getLanguageHandler()
                        .getMessage(
                           "welcome_msg",
                           new Pair("online_name", ((GameProfile)pair.getValue1()).getName()),
                           new Pair("online_uuid", ((GameProfile)pair.getValue1()).getId()),
                           new Pair("service_name", ((BaseServiceConfig)pair.getValue2()).getName()),
                           new Pair("service_id", ((BaseServiceConfig)pair.getValue2()).getId()),
                           new Pair("profile_name", player.getName()),
                           new Pair("profile_uuid", player.getUniqueId())
                        );
                  }

                  player.sendMessagePL(msg);
               },
               3000L
            );
      }
   }

   public RSLBPlayerData getPlayerData(UUID inGameUUID) {
      return this.cache.get(inGameUUID);
   }

    public Pair<GameProfile, Integer> getPlayerOnlineProfile(UUID inGameUUID) {
       PlayerHandler.Entry entry = this.cache.get(inGameUUID);
       return entry == null ? null : new Pair<>(entry.onlineProfile, entry.serviceConfig.getId());
    }

    private Pair<GameProfile, BaseServiceConfig> getPlayerOnlineProfile0(UUID inGameUUID) {
       PlayerHandler.Entry entry = this.cache.get(inGameUUID);
       return entry == null ? null : new Pair<>(entry.onlineProfile, entry.serviceConfig);
    }

   public UUID getInGameUUID(UUID onlineUUID, int serviceId) {
      for (Map.Entry<UUID, PlayerHandler.Entry> entry : this.cache.entrySet()) {
         if (entry.getValue().onlineProfile.getId().equals(onlineUUID) && entry.getValue().serviceConfig.getId() == serviceId) {
            return entry.getKey();
         }
      }

      return null;
   }

   public String getServiceName(int serviceId) {
      BaseServiceConfig config = this.core.getPluginConfig().getServiceIdMap().get(serviceId);
      return config == null ? null : config.getName();
   }

   public void register() {
      this.core
         .getPlugin()
         .getRunServer()
         .getScheduler()
         .runTaskAsyncTimer(
            () -> {
               Set<UUID> onlinePlayerUUIDs = this.core
                  .getPlugin()
                  .getRunServer()
                  .getPlayerManager()
                  .getOnlinePlayers()
                  .stream()
                  .<UUID>map(IPlayer::getUniqueId)
                  .collect(Collectors.toSet());
               Set<Map.Entry<UUID, PlayerHandler.Entry>> noExists = this.cache
                  .entrySet()
                  .stream()
                  .filter(ex -> !onlinePlayerUUIDs.contains(ex.getKey()))
                  .collect(Collectors.toSet());

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException e) {
                  LoggerProvider.getLogger().error("An exception occurred on the delayed cache clearing.", e);
               }

               for (Map.Entry<UUID, PlayerHandler.Entry> e : noExists) {
                  PlayerHandler.Entry entry = this.cache.get(e.getKey());
                  if (entry != null && e.getValue().equals(entry)) {
                     this.cache.remove(e.getKey());
                  }
               }
            },
            0L,
            60000L
         );
   }

   @Generated
   public Map<UUID, PlayerHandler.Entry> getLoginCache() {
      return this.loginCache;
   }

   public static class Entry implements RSLBPlayerData {
      private final GameProfile onlineProfile;
      private final BaseServiceConfig serviceConfig;
      private final long signTimeMillis;

      @NotNull
      public GameProfile getOnlineProfile() {
         return this.onlineProfile;
      }

      @NotNull
      public IService getLoginService() {
         return this.serviceConfig;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            PlayerHandler.Entry entry = (PlayerHandler.Entry)o;
            return Objects.equals(this.serviceConfig, entry.serviceConfig)
               && this.signTimeMillis == entry.signTimeMillis
               && Objects.equals(this.onlineProfile, entry.onlineProfile);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.onlineProfile, this.serviceConfig, this.signTimeMillis);
      }

      @Generated
      public Entry(GameProfile onlineProfile, BaseServiceConfig serviceConfig, long signTimeMillis) {
         this.onlineProfile = onlineProfile;
         this.serviceConfig = serviceConfig;
         this.signTimeMillis = signTimeMillis;
      }
   }
}
