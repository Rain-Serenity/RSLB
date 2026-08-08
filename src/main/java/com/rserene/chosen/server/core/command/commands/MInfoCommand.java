package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashSet;
import java.util.Set;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.OnlinePlayerArgumentType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public class MInfoCommand {
   private final CommandHandler handler;

   public MInfoCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      // 反编译器生成的 raw 泛型链会让 requires 的入参退化为 Object，这里恢复 Brigadier 的强类型构建链。
      RequiredArgumentBuilder<ISender, Set<IPlayer>> playerArgument = this.handler
         .argument("player", OnlinePlayerArgumentType.players())
         .requires(iSender -> iSender.hasPermission("rslb.info.other"))
         .executes(this::executeInfo);
      return literalArgumentBuilder
         .then(playerArgument)
         .requires(iSender -> iSender.hasPermission("rslb.info.oneself") || iSender.hasPermission("rslb.info.other"))
         .executes(this::executeInfoOneself);
   }

   private int executeInfo(CommandContext<ISender> context) {
      Set<IPlayer> players = OnlinePlayerArgumentType.getPlayers(context, "player");
      this.processInfoCommand(context, players);
      return 0;
   }

   private int executeInfoOneself(CommandContext<ISender> context) throws CommandSyntaxException {
      this.handler.requirePlayer(context);
      IPlayer player = ((ISender)context.getSource()).getAsPlayer();
      HashSet<IPlayer> players = new HashSet<>();
      players.add(player);
      this.processInfoCommand(context, players);
      return 0;
   }

   private void processInfoCommand(CommandContext<ISender> context, Set<IPlayer> players) {
      if (players.size() > 1) {
         ((ISender)context.getSource())
            .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_info_multi", new Pair("size", players.size())));
      }

      for (IPlayer player : players) {
         Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
         if (profile == null) {
            ((ISender)context.getSource())
               .sendMessagePL(
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

            ((ISender)context.getSource())
               .sendMessagePL(
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
