package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.StringArgumentType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public class RootCommand {
   private final CommandHandler handler;

   public RootCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(
                                       ((LiteralArgumentBuilder)this.handler
                                             .literal("reload")
                                             .requires(sender -> sender.hasPermission("command.RSLB.reload")))
                                          .executes(this::executeReload)
                                    ))
                                    .then(
                                       ((LiteralArgumentBuilder)this.handler
                                             .literal("eraseUsername")
                                             .requires(sender -> sender.hasPermission("command.RSLB.eraseusername")))
                                          .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeEraseUsername))
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)this.handler
                                          .literal("eraseAllUsernames")
                                          .requires(iSender -> iSender.hasPermission("command.RSLB.eraseallusernames")))
                                       .executes(this::executeEraseAllUsernames)
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)this.handler.literal("confirm").requires(sender -> sender.hasPermission("command.RSLB.confirm")))
                                    .executes(this::executeConfirm)
                              ))
                           .then(
                              ((LiteralArgumentBuilder)this.handler.literal("list").requires(sender -> sender.hasPermission("command.RSLB.list")))
                                 .executes(this::executeList)
                           ))
                        .then(new MWhitelistCommand(this.handler).register(this.handler.literal("whitelist"))))
                     .then(new MProfileCommand(this.handler).register(this.handler.literal("profile"))))
                  .then(new MRenameCommand(this.handler).register(this.handler.literal("rename"))))
               .then(new MFindCommand(this.handler).register(this.handler.literal("find"))))
            .then(new MInfoCommand(this.handler).register(this.handler.literal("info"))))
          .then(new MLinkCommand(this.handler).register(this.handler.literal("link")))
          .then(new MHelpCommand(this.handler).register(this.handler.literal("help")));
   }

   private int executeList(CommandContext<ISender> context) {
      Set<IPlayer> onlinePlayers = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers();
      Map<Integer, List<IPlayer>> identifiedPlayerMap = new HashMap<>();

      for (IPlayer player : onlinePlayers) {
         Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
         int sid = -1;
         if (profile != null) {
            sid = (Integer)profile.getValue2();
         }

         List<IPlayer> list = identifiedPlayerMap.getOrDefault(sid, new ArrayList<>());
         list.add(player);
         identifiedPlayerMap.put(sid, list);
      }

      CommandHandler.getCore().getPluginConfig().getServiceIdMap().forEach((key, value) -> {
         if (!identifiedPlayerMap.containsKey(key)) {
            identifiedPlayerMap.put(key, new ArrayList<>());
         }
      });
      String message = CommandHandler.getCore()
         .getLanguageHandler()
         .getMessage(
            "command_message_list",
            new Pair(
               "list",
               identifiedPlayerMap.entrySet()
                  .stream()
                  .map(
                     entry -> {
                        String sname;
                        if (entry.getKey() == -1) {
                           sname = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_unidentified_entry_name");
                        } else {
                           BaseServiceConfig baseServiceConfig = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(entry.getKey());
                           if (baseServiceConfig == null) {
                              sname = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_unknown_entry_name");
                           } else {
                              sname = baseServiceConfig.getName();
                           }
                        }

                        String playerListString = entry.getValue()
                           .stream()
                           .map(
                              s -> CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_player_entry", new Pair("name", s.getName()))
                           )
                           .collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_player_delimiter")));
                        return CommandHandler.getCore()
                           .getLanguageHandler()
                           .getMessage(
                              "command_message_list_entry",
                              new Pair("service_name", sname),
                              new Pair("service_id", entry.getKey()),
                              new Pair("count", entry.getValue().size()),
                              new Pair("list", playerListString)
                           );
                     }
                  )
                  .collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_list_delimiter")))
            ),
            new Pair("count", onlinePlayers.size())
         );
      ((ISender)context.getSource()).sendMessagePL(message);
      return 0;
   }

   private int executeEraseAllUsernames(CommandContext<ISender> context) {
      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            (ISender)context.getSource(),
            () -> {
               int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseAllUsername();
               String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy_all");
               CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickAll(kickMsg);
               ((ISender)context.getSource())
                  .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_done", new Pair("count", i)));
            },
            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_desc"),
            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_cq")
         );
      return 0;
   }

   private int executeConfirm(CommandContext<ISender> context) {
      try {
         this.handler.getSecondaryConfirmationHandler().confirm((ISender)context.getSource());
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeEraseUsername(CommandContext<ISender> context) {
      try {
         String string = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
         UUID ignoreCase = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(string);
         if (ignoreCase == null) {
            ((ISender)context.getSource())
               .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none", new Pair("name", string)));
            return 0;
         } else {
            this.handler
               .getSecondaryConfirmationHandler()
               .submit(
                  (ISender)context.getSource(),
                  () -> {
                     int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
                     String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy", new Pair("name", string));
                     CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(string, kickMsg);
                     if (i == 0) {
                        ((ISender)context.getSource())
                           .sendMessagePL(
                              CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none", new Pair("name", string))
                           );
                     } else {
                        ((ISender)context.getSource())
                           .sendMessagePL(
                              CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_done", new Pair("name", string))
                           );
                     }
                  },
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_desc", new Pair("name", string)),
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_cq", new Pair("name", string))
               );
            return 0;
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeReload(CommandContext<ISender> context) {
      try {
         CommandHandler.getCore().reload();
         ((ISender)context.getSource()).sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
