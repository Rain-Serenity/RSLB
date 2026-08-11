package com.rserene.chosen.server.command.commands;

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
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.argument.StringArgumentType;
import com.rserene.chosen.server.config.service.BaseServiceConfig;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RootCommand {
   private final CommandHandler handler;

   public RootCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<CommandSender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(
                                       ((LiteralArgumentBuilder)this.handler
                                             .literal("reload")
                                             .requires(sender -> sender.hasPermission("rslb.reload")))
                                          .executes(this::executeReload)
                                    ))
                                    .then(
                                       ((LiteralArgumentBuilder)this.handler
                                             .literal("eraseUsername")
                                             .requires(sender -> sender.hasPermission("rslb.erase.username")))
                                          .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeEraseUsername))
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)this.handler
                                          .literal("eraseAllUsernames")
                                          .requires(iSender -> iSender.hasPermission("rslb.erase.all")))
                                       .executes(this::executeEraseAllUsernames)
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)this.handler.literal("confirm").requires(sender -> sender.hasPermission("rslb.confirm")))
                                    .executes(this::executeConfirm)
                              ))
                           .then(
                              ((LiteralArgumentBuilder)this.handler.literal("list").requires(sender -> sender.hasPermission("rslb.list")))
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

   private int executeList(CommandContext<CommandSender> context) {
      Set<Player> onlinePlayers = MessageUtil.getOnlinePlayers();
      Map<Integer, List<Player>> identifiedPlayerMap = new HashMap<>();

      for (Player player : onlinePlayers) {
         Pair<GameProfile, Integer> profile = CommandHandler.getCore().getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
         int sid = -1;
         if (profile != null) {
            sid = (Integer)profile.getValue2();
         }

         List<Player> list = identifiedPlayerMap.getOrDefault(sid, new ArrayList<>());
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
      MessageUtil.sendLegacy(context.getSource(), message);
      return 0;
   }

   private int executeEraseAllUsernames(CommandContext<CommandSender> context) {
      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            context.getSource(),
            () -> {
               int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseAllUsername();
               String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy_all");
               MessageUtil.kickAll(kickMsg);
               MessageUtil.sendLegacy(
                  context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_done", new Pair("count", i))
               );
            },
            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_desc"),
            CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_all_username_cq")
         );
      return 0;
   }

   private int executeConfirm(CommandContext<CommandSender> context) {
      try {
         this.handler.getSecondaryConfirmationHandler().confirm(context.getSource());
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeEraseUsername(CommandContext<CommandSender> context) {
      try {
         String string = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
         UUID ignoreCase = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(string);
         if (ignoreCase == null) {
            MessageUtil.sendLegacy(
               context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none", new Pair("name", string))
            );
            return 0;
         } else {
            this.handler
               .getSecondaryConfirmationHandler()
               .submit(
                  context.getSource(),
                  () -> {
                     int i = CommandHandler.getCore().getSqlManager().getInGameProfileTable().eraseUsername(string);
                     String kickMsg = CommandHandler.getCore().getLanguageHandler().getMessage("in_game_username_occupy", new Pair("name", string));
                     MessageUtil.kickPlayerIfOnline(string, kickMsg);
                     if (i == 0) {
                        MessageUtil.sendLegacy(
                           context.getSource(),
                           CommandHandler.getCore().getLanguageHandler().getMessage("command_message_erase_username_none", new Pair("name", string))
                        );
                     } else {
                        MessageUtil.sendLegacy(
                           context.getSource(),
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
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeReload(CommandContext<CommandSender> context) {
      try {
         CommandHandler.getCore().reload();
         MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_reloaded"));
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }
}