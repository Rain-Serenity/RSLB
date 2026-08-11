package com.rserene.chosen.server.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashSet;
import java.util.Set;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.argument.OnlinePlayerArgumentType;
import com.rserene.chosen.server.config.service.BaseServiceConfig;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MInfoCommand {
   private final CommandHandler handler;

   public MInfoCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
      // 使用强类型的参数构建链，避免 requires 的入参类型退化为 Object。
      RequiredArgumentBuilder<CommandSender, Set<Player>> playerArgument = this.handler
         .argument("player", OnlinePlayerArgumentType.players())
         .requires(iSender -> iSender.hasPermission("rslb.info.other"))
         .executes(this::executeInfo);
      return literalArgumentBuilder
         .then(playerArgument)
         .requires(iSender -> iSender.hasPermission("rslb.info.oneself") || iSender.hasPermission("rslb.info.other"))
         .executes(this::executeInfoOneself);
   }

   private int executeInfo(CommandContext<CommandSender> context) {
      Set<Player> players = OnlinePlayerArgumentType.getPlayers(context, "player");
      this.processInfoCommand(context, players);
      return 0;
   }

   private int executeInfoOneself(CommandContext<CommandSender> context) throws CommandSyntaxException {
      this.handler.requirePlayer(context);
      Player player = (Player)context.getSource();
      HashSet<Player> players = new HashSet<>();
      players.add(player);
      this.processInfoCommand(context, players);
      return 0;
   }

   private void processInfoCommand(CommandContext<CommandSender> context, Set<Player> players) {
      if (players.size() > 1) {
         MessageUtil.sendLegacy(
            context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_multi", new Pair("size", players.size()))
         );
      }

      for (Player player : players) {
         Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
         if (profile == null) {
            MessageUtil.sendLegacy(
               context.getSource(),
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage("command_message_info_unknown", new Pair("name", player.getName()), new Pair("uuid", player.getUniqueId()))
            );
         } else {
            BaseServiceConfig bsc = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(profile.getValue2());
            String serviceName;
            if (bsc == null) {
               serviceName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_unidentified_name");
            } else {
               serviceName = bsc.getName();
            }

            MessageUtil.sendLegacy(
               context.getSource(),
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_info",
                     new Pair("name", player.getName()),
                     new Pair("uuid", player.getUniqueId()),
                     new Pair("service_name", serviceName),
                     new Pair("service_id", (Integer)profile.getValue2()),
                     new Pair("online_name", ((GameProfile)profile.getValue1()).getName()),
                     new Pair("online_uuid", ((GameProfile)profile.getValue1()).getId())
                  )
            );
         }
      }
   }
}