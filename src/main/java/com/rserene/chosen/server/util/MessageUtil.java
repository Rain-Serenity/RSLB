package com.rserene.chosen.server.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.rserene.chosen.server.RSLB;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 原生 Bukkit 玩家/发送者工具。
 *
 * 统一 legacy '&' 颜色码解析发送（玩家端渲染为彩色文本，控制台由 Paper 渲染为 ANSI 颜色）；
 * 踢出经 RegionScheduler 调度到玩家所在线程（Paper 主线程 / Folia 区域线程），任务内部再次校验在线状态；
 * 在线玩家查询与 IPlayerManager 原语义一致（名称大小写不敏感匹配、返回快照集合）。
 */
public final class MessageUtil {
   private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

   private MessageUtil() {
   }

   public static void sendLegacy(CommandSender sender, String message) {
      for (String line : message.split("\\r?\\n")) {
         sender.sendMessage(LEGACY.deserialize(line));
      }
   }

   public static void kick(Player player, String message) {
      Player target = player;
      Location location = target.getLocation();
      Bukkit.getRegionScheduler().run(RSLB.getInstance(), location, task -> {
         if (target.isOnline()) {
            target.kick(LEGACY.deserialize(message));
         }
      });
   }

   public static Set<Player> getPlayers(String name) {
      return Bukkit.getOnlinePlayers().stream()
         .filter(p -> p.getName().equalsIgnoreCase(name))
         .collect(java.util.stream.Collectors.toSet());
   }

   public static Set<Player> getOnlinePlayers() {
      return new HashSet<>(Bukkit.getOnlinePlayers());
   }

   public static Player getPlayer(UUID uuid) {
      return Bukkit.getPlayer(uuid);
   }

   public static void kickPlayerIfOnline(UUID uuid, String message) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         kick(player, message);
      }
   }

   public static void kickPlayerIfOnline(String name, String message) {
      for (Player player : getPlayers(name)) {
         kick(player, message);
      }
   }

   public static void kickAll(String message) {
      for (Player player : getOnlinePlayers()) {
         kick(player, message);
      }
   }
}
