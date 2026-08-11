package com.rserene.chosen.server.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.util.ValueUtil;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.argument.OnlineArgumentType;
import com.rserene.chosen.server.command.argument.ProfileArgumentType;
import com.rserene.chosen.server.command.argument.StringArgumentType;
import com.rserene.chosen.server.command.argument.UUIDArgumentType;
import com.rserene.chosen.server.main.RSLBCore;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MProfileCommand {
   private final CommandHandler handler;

   public MProfileCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<CommandSender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder
            .requires(iSender ->
               iSender.hasPermission("rslb.profile.create")
               || iSender.hasPermission("rslb.profile.set.oneself")
               || iSender.hasPermission("rslb.profile.set.other")
               || iSender.hasPermission("rslb.profile.remove"))
            .then(
               ((LiteralArgumentBuilder)this.handler.literal("create").requires(iSender -> iSender.hasPermission("rslb.profile.create")))
                  .then(
                     ((RequiredArgumentBuilder)this.handler
                           .argument("username", StringArgumentType.string())
                           .then(this.handler.argument("ingameuuid", UUIDArgumentType.uuid()).executes(this::executeCreate)))
                        .executes(this::executeCreateRandomUUID)
                  )
            ))
            .then(
               ((LiteralArgumentBuilder)this.handler
                     .literal("set")
                     .requires(iSender ->
                        iSender.hasPermission("rslb.profile.set.oneself")
                        || iSender.hasPermission("rslb.profile.set.other"))
                     .then(
                        ((RequiredArgumentBuilder)this.handler
                              .argument("profile", ProfileArgumentType.profile())
                              .requires(iSender -> iSender.hasPermission("rslb.profile.set.oneself")))
                           .executes(this::executeSetOneself)
                     ))
                  .then(
                     this.handler
                        .argument("profile", ProfileArgumentType.profile())
                        .then(
                           ((RequiredArgumentBuilder)this.handler
                                 .argument("online", OnlineArgumentType.online())
                                 .requires(iSender -> iSender.hasPermission("rslb.profile.set.other")))
                              .executes(this::executeSetOther)
                        )
                  )
            ))
         .then(
            this.handler
               .literal("remove")
               .then(
                  ((RequiredArgumentBuilder)this.handler
                        .argument("profile", ProfileArgumentType.profile())
                        .requires(iSender -> iSender.hasPermission("rslb.profile.remove")))
                     .executes(this::executeRemove)
               )
         );
   }

   private int executeRemove(CommandContext<CommandSender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         String name = Optional.ofNullable(profile.getProfileName())
            .orElse(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_unnamed"));
         this.handler
            .getSecondaryConfirmationHandler()
            .submit(
               context.getSource(),
               () -> {
                  CommandHandler.getCore().getSqlManager().getInGameProfileTable().remove(profile.getProfileUUID());
                  MessageUtil.sendLegacy(
                     context.getSource(),
                     CommandHandler.getCore()
                        .getLanguageHandler()
                        .getMessage("command_message_profile_remove_succeed", new Pair("name", name), new Pair("uuid", profile.getProfileUUID()))
                  );
                  Player player = Bukkit.getPlayer(profile.getProfileUUID());
                  if (player != null) {
                     MessageUtil.kick(player, CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_kickmessage"));
                  }
               },
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage("command_message_profile_remove_desc", new Pair("name", name), new Pair("uuid", profile.getProfileUUID())),
               CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_cq")
            );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeSetOther(CommandContext<CommandSender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         this.processSet(context, online.getOnlineUUID(), online.getOnlineName(), online.getBaseServiceConfig().getId(), profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeSetOneself(CommandContext<CommandSender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         Pair<GameProfile, Integer> pair = this.handler.requireDataCacheArgumentSelf(context);
         this.processSet(context, ((GameProfile)pair.getValue1()).getId(), ((GameProfile)pair.getValue1()).getName(), (Integer)pair.getValue2(), profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private void processSet(CommandContext<CommandSender> context, UUID from, String fromName, int serviceId, ProfileArgumentType.ProfileArgument to) {
      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            context.getSource(),
            () -> {
               CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(from, serviceId, to.getProfileUUID());
               MessageUtil.sendLegacy(
                  context.getSource(),
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_profile_set_succeed",
                        new Pair("redirect_name", to.getProfileName()),
                        new Pair("redirect_uuid", to.getProfileUUID()),
                        new Pair("online_uuid", from),
                        new Pair("online_name", fromName)
                     )
               );
               UUID inGameUUID = CommandHandler.getCore().getPlayerHandler().getInGameUUID(from, serviceId);
               if (inGameUUID != null) {
                  MessageUtil.kickPlayerIfOnline(
                     inGameUUID,
                     CommandHandler.getCore()
                        .getLanguageHandler()
                        .getMessage(
                           "command_message_profile_set_succeed_kickmessage",
                           new Pair("redirect_name", to.getProfileName()),
                           new Pair("redirect_uuid", to.getProfileUUID()),
                           new Pair("online_uuid", from),
                           new Pair("online_name", fromName)
                        )
                  );
               }
            },
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage(
                  "command_message_profile_set_desc",
                  new Pair("redirect_name", to.getProfileName()),
                  new Pair("redirect_uuid", to.getProfileUUID()),
                  new Pair("online_uuid", from),
                  new Pair("online_name", fromName)
               ),
            CommandHandler.getCore()
               .getLanguageHandler()
               .getMessage(
                  "command_message_profile_set_cq",
                  new Pair("redirect_name", to.getProfileName()),
                  new Pair("redirect_uuid", to.getProfileUUID()),
                  new Pair("online_uuid", from),
                  new Pair("online_name", fromName)
               )
         );
   }

   private void processCreate(CommandContext<CommandSender> context, String name, UUID uuid) throws SQLException {
      RSLBCore core = CommandHandler.getCore();
      String nameAllowedRegular = core.getPluginConfig().getNameAllowedRegular();
      if (!ValueUtil.isEmpty(nameAllowedRegular) && !Pattern.matches(nameAllowedRegular, name)) {
         MessageUtil.sendLegacy(
            context.getSource(),
            core.getLanguageHandler()
               .getMessage("command_message_profile_create_namemismatch", new Pair("name", name), new Pair("regular", nameAllowedRegular))
         );
      } else if (uuid.version() < 2) {
         MessageUtil.sendLegacy(context.getSource(), core.getLanguageHandler().getMessage("command_message_profile_create_uuidmismatch", new Pair("uuid", uuid)));
      } else {
         Pair<UUID, String> pair = core.getSqlManager().getInGameProfileTable().get(uuid);
         if (pair != null) {
            MessageUtil.sendLegacy(
               context.getSource(),
               core.getLanguageHandler()
                  .getMessage("command_message_profile_create_uuidoccupied", new Pair("uuid", uuid), new Pair("name", (String)pair.getValue2()))
            );
         } else {
            UUID uuidIgnoreCase = core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(name);
            if (uuidIgnoreCase != null) {
               MessageUtil.sendLegacy(
                  context.getSource(),
                  core.getLanguageHandler()
                     .getMessage("command_message_profile_create_nameoccupied", new Pair("name", name), new Pair("uuid", uuidIgnoreCase))
               );
            } else {
               core.getSqlManager().getInGameProfileTable().insertNewData(uuid, name);
               MessageUtil.sendLegacy(context.getSource(), core.getLanguageHandler().getMessage("command_message_profile_create", new Pair("uuid", name), new Pair("name", uuid)));
            }
         }
      }
   }

   private int executeCreate(CommandContext<CommandSender> context) {
      try {
         String username = StringArgumentType.getString(context, "username");
         UUID ingameuuid = UUIDArgumentType.getUuid(context, "ingameuuid");
         this.processCreate(context, username, ingameuuid);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeCreateRandomUUID(CommandContext<CommandSender> context) {
      try {
         String username = StringArgumentType.getString(context, "username");
         UUID ingameuuid = UUID.randomUUID();
         this.processCreate(context, username, ingameuuid);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }
}