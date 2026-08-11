package com.rserene.chosen.server.player;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Generated;
import com.rserene.chosen.server.player.RSLBPlayerData;
import com.rserene.chosen.server.player.HandleResult;
import com.rserene.chosen.server.player.HandlerAPI;
import com.rserene.chosen.server.player.HandleResult.Type;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.service.IService;
import com.rserene.chosen.server.config.service.BaseServiceConfig;
import com.rserene.chosen.server.main.RSLBCore;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
         return new HandleResult(Type.KICK, this.core.getLanguageHandler().getMessage("auth_handler_need_use_login"));
      } else {
         long l = System.currentTimeMillis() - remove.signTimeMillis;
         if (l > 5000L) {
            this.core
               .getLogger()
               .warning(
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

   public void callPlayerJoinGame(Player player) {
      if (this.core.getPluginConfig().isWelcomeMsg()) {
         Bukkit.getAsyncScheduler()
            .runDelayed(
               this.core.getPlugin(),
               task -> {
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
                           new Pair("online_name", pair.getValue1().getName()),
                           new Pair("online_uuid", pair.getValue1().getId()),
                           new Pair("service_name", pair.getValue2().getName()),
                           new Pair("service_id", pair.getValue2().getId()),
                           new Pair("profile_name", player.getName()),
                           new Pair("profile_uuid", player.getUniqueId())
                        );
                  }

                  MessageUtil.sendLegacy(player, msg);
               },
               3000L,
               TimeUnit.MILLISECONDS
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
      Bukkit.getAsyncScheduler()
         .runAtFixedRate(
            this.core.getPlugin(),
            task -> {
               Set<UUID> onlinePlayerUUIDs = Bukkit.getOnlinePlayers()
                  .stream()
                  .<UUID>map(Player::getUniqueId)
                  .collect(Collectors.toSet());
               Set<Map.Entry<UUID, PlayerHandler.Entry>> noExists = this.cache
                  .entrySet()
                  .stream()
                  .filter(ex -> !onlinePlayerUUIDs.contains(ex.getKey()))
                  .collect(Collectors.toSet());

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException e) {
                  this.core.getLogger().log(Level.SEVERE, "An exception occurred on the delayed cache clearing.", e);
               }

               for (Map.Entry<UUID, PlayerHandler.Entry> e : noExists) {
                  PlayerHandler.Entry entry = this.cache.get(e.getKey());
                  if (entry != null && e.getValue().equals(entry)) {
                     this.cache.remove(e.getKey());
                  }
               }
            },
            0L,
            60L,
            TimeUnit.SECONDS
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