package com.rserene.chosen.server.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.argument.OnlineArgumentType;
import com.rserene.chosen.server.command.argument.StringArgumentType;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MWhitelistCommand {
   private final CommandHandler handler;

   public MWhitelistCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<CommandSender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder
            .requires(sender ->
               sender.hasPermission("rslb.whitelist.add")
               || sender.hasPermission("rslb.whitelist.remove")
               || sender.hasPermission("rslb.whitelist.specific.add")
               || sender.hasPermission("rslb.whitelist.specific.remove")
               || sender.hasPermission("rslb.whitelist.list")
               || sender.hasPermission("rslb.whitelist.list.verbose"))
            .then(
                  ((LiteralArgumentBuilder)this.handler.literal("add").requires(sender -> sender.hasPermission("rslb.whitelist.add")))
                     .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeAddUsername))
               ))
               .then(
                  ((LiteralArgumentBuilder)this.handler.literal("remove").requires(sender -> sender.hasPermission("rslb.whitelist.remove")))
                     .then(this.handler.argument("username", StringArgumentType.string()).executes(this::executeRemoveUsername))
               ))
            .then(
               ((LiteralArgumentBuilder)this.handler
                     .literal("specific")
                     .requires(sender ->
                        sender.hasPermission("rslb.whitelist.specific.add")
                        || sender.hasPermission("rslb.whitelist.specific.remove"))
                     .then(
                        ((LiteralArgumentBuilder)this.handler.literal("add").requires(sender -> sender.hasPermission("rslb.whitelist.specific.add")))
                           .then(this.handler.argument("online", OnlineArgumentType.online()).executes(this::executeAdd))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)this.handler
                           .literal("remove")
                           .requires(sender -> sender.hasPermission("rslb.whitelist.specific.remove")))
                        .then(this.handler.argument("online", OnlineArgumentType.online()).executes(this::executeRemove))
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)this.handler
                     .literal("list")
                     .requires(sender -> sender.hasPermission("rslb.whitelist.list")))
                  .executes(this::executeList))
               .then(
                  ((LiteralArgumentBuilder)this.handler.literal("verbose").requires(sender -> sender.hasPermission("rslb.whitelist.list.verbose")))
                     .executes(this::executeListVerbose)
               )
         );
   }

   private int executeRemove(CommandContext<CommandSender> context) {
      try {
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         if (!online.isWhitelist()) {
            MessageUtil.sendLegacy(
               context.getSource(),
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
         MessageUtil.sendLegacy(
            context.getSource(),
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
            MessageUtil.kickPlayerIfOnline(inGameUUID, CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
         }

         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeAdd(CommandContext<CommandSender> context) {
      try {
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         if (online.isWhitelist()) {
            MessageUtil.sendLegacy(
               context.getSource(),
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
         MessageUtil.sendLegacy(
            context.getSource(),
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
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeRemoveUsername(CommandContext<CommandSender> context) {
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
            MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat", new Pair("name", username)));
            return 0;
         }

         MessageUtil.sendLegacy(
            context.getSource(),
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage("command_message_whitelist_remove", new Pair("name", username), new Pair("count", count))
         );
         if (inGameUUID != null) {
            Player player = Bukkit.getPlayer(inGameUUID);
            if (player != null) {
               MessageUtil.kick(player, CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
            }
         }

         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeAddUsername(CommandContext<CommandSender> context) {
      try {
         String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
         boolean have = false;
         UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
         if (inGameUUID != null) {
            have = CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
         }

         if (have) {
            MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", new Pair("name", username)));
            return 0;
         } else if (!CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().add(username)) {
            MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat", new Pair("name", username)));
            return 0;
         } else {
            MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add", new Pair("name", username)));
            return 0;
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeList(CommandContext<CommandSender> context, boolean verbose) {
      try {
         List<String> list = CommandHandler.getCore().getSqlManager().getUserDataTable().listWhitelist(verbose);
         MessageUtil.sendLegacy(
            context.getSource(),
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage(
                  "command_message_whitelist_list_table", new Pair("count", list.size()), new Pair("list", String.join(verbose ? ", \n" : ", ", list))
               )
         );
         Set<String> cache = CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist();
         MessageUtil.sendLegacy(
            context.getSource(),
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
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeList(CommandContext<CommandSender> context) {
      try {
         return this.executeList(context, false);
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeListVerbose(CommandContext<CommandSender> context) {
      try {
         return this.executeList(context, true);
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }
}