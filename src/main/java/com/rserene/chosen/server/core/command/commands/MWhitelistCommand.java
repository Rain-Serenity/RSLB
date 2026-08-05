package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.OnlineArgumentType;
import com.rserene.chosen.server.core.command.argument.StringArgumentType;

public class MWhitelistCommand {
   private final CommandHandler handler;

   public MWhitelistCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(
                  ((LiteralArgumentBuilder)this.handler.literal("add").requires(sender -> sender.hasPermission("command.RSLV.whitelist.add")))
                     .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeAddUsername))
               ))
               .then(
                  ((LiteralArgumentBuilder)this.handler.literal("remove").requires(sender -> sender.hasPermission("command.RSLV.whitelist.remove")))
                     .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeRemoveUsername))
               ))
            .then(
               ((LiteralArgumentBuilder)this.handler
                     .literal("specific")
                     .then(
                        ((LiteralArgumentBuilder)this.handler.literal("add").requires(sender -> sender.hasPermission("command.RSLV.whitelist.specific.add")))
                           .then(this.handler.argument("online", OnlineArgumentType.online()).executes(this::executeAdd))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)this.handler
                           .literal("remove")
                           .requires(sender -> sender.hasPermission("command.RSLV.whitelist.specific.remove")))
                        .then(this.handler.argument("online", OnlineArgumentType.online()).executes(this::executeRemove))
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)this.handler
                     .literal("list")
                     .requires(sender -> sender.hasPermission("command.RSLV.whitelist.list")))
                  .executes(this::executeList))
               .then(
                  ((LiteralArgumentBuilder)this.handler.literal("verbose").requires(sender -> sender.hasPermission("command.RSLV.whitelist.list.verbose")))
                     .executes(this::executeListVerbose)
               )
         );
   }

   private int executeRemove(CommandContext<ISender> context) {
      try {
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         if (!online.isWhitelist()) {
            ((ISender)context.getSource())
               .sendMessagePL(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_whitelist_permanent_remove_repeat",
                        new Pair("online_uuid", online.getOnlineUUID()),
                        new Pair("online_name", online.getOnlineName()),
                        new Pair("service_name", online.getBaseServiceConfig().getName()),
                        new Pair("service_id", online.getBaseServiceConfig().getId())
                     )
               );
            return 0;
         }

         CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), false);
         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_whitelist_permanent_remove",
                     new Pair("online_uuid", online.getOnlineUUID()),
                     new Pair("online_name", online.getOnlineName()),
                     new Pair("service_name", online.getBaseServiceConfig().getName()),
                     new Pair("service_id", online.getBaseServiceConfig().getId())
                  )
            );
         UUID inGameUUID = CommandHandler.getCore()
            .getSqlManager()
            .getUserDataTable()
            .getInGameUUID(online.getOnlineUUID(), online.getBaseServiceConfig().getId());
         if (inGameUUID != null) {
            CommandHandler.getCore()
               .getPlugin()
               .getRunServer()
               .getPlayerManager()
               .kickPlayerIfOnline(inGameUUID, CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
         }

         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeAdd(CommandContext<ISender> context) {
      try {
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         if (online.isWhitelist()) {
            ((ISender)context.getSource())
               .sendMessagePL(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_whitelist_permanent_add_repeat",
                        new Pair("online_uuid", online.getOnlineUUID()),
                        new Pair("online_name", online.getOnlineName()),
                        new Pair("service_name", online.getBaseServiceConfig().getName()),
                        new Pair("service_id", online.getBaseServiceConfig().getId())
                     )
               );
            return 0;
         }

         if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(online.getOnlineUUID(), online.getBaseServiceConfig().getId())) {
            CommandHandler.getCore()
               .getSqlManager()
               .getUserDataTable()
               .insertNewData(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), null, null);
         }

         CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), true);
         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_whitelist_permanent_add",
                     new Pair("online_uuid", online.getOnlineUUID()),
                     new Pair("online_name", online.getOnlineName()),
                     new Pair("service_name", online.getBaseServiceConfig().getName()),
                     new Pair("service_id", online.getBaseServiceConfig().getId())
                  )
            );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeRemoveUsername(CommandContext<ISender> context) {
      try {
         String username = StringArgumentType.getString(context, "username");
         int count = 0;
         if (CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().remove(username)) {
            count++;
         }

         UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
         if (inGameUUID != null && CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID)) {
            count++;
            CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(inGameUUID, false);
         }

         if (count == 0) {
            ((ISender)context.getSource())
               .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat", new Pair("name", username)));
            return 0;
         }

         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage("command_message_whitelist_remove", new Pair("name", username), new Pair("count", count))
            );
         if (inGameUUID != null) {
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
            if (player != null) {
               player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
            }
         }

         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeAddUsername(CommandContext<ISender> context) {
      try {
         String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
         boolean have = false;
         UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
         if (inGameUUID != null) {
            have = CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
         }

         if (have) {
            ((ISender)context.getSource())
               .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", new Pair("name", username)));
            return 0;
         } else if (!CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().add(username)) {
            ((ISender)context.getSource())
               .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", new Pair("name", username)));
            return 0;
         } else {
            ((ISender)context.getSource())
               .sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add", new Pair("name", username)));
            return 0;
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeList(CommandContext<ISender> context, boolean verbose) {
      try {
         List<String> list = CommandHandler.getCore().getSqlManager().getUserDataTable().listWhitelist(verbose);
         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_whitelist_list_table", new Pair("count", list.size()), new Pair("list", String.join(verbose ? ", \n" : ", ", list))
                  )
            );
         Set<String> cache = CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist();
         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_whitelist_list_cache",
                     new Pair("list", cache.stream().collect(Collectors.joining(", "))),
                     new Pair("count", cache.size())
                  )
            );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeList(CommandContext<ISender> context) {
      try {
         return this.executeList(context, false);
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeListVerbose(CommandContext<ISender> context) {
      try {
         return this.executeList(context, true);
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
