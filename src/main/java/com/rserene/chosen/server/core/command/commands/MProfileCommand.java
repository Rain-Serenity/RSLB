package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.OnlineArgumentType;
import com.rserene.chosen.server.core.command.argument.ProfileArgumentType;
import com.rserene.chosen.server.core.command.argument.StringArgumentType;
import com.rserene.chosen.server.core.command.argument.UUIDArgumentType;
import com.rserene.chosen.server.core.main.RSLVCore;

public class MProfileCommand {
   private final CommandHandler handler;

   public MProfileCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(
               ((LiteralArgumentBuilder)this.handler.literal("create").requires(iSender -> iSender.hasPermission("command.RSLB.profile.create")))
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
                     .then(
                        ((RequiredArgumentBuilder)this.handler
                              .argument("profile", ProfileArgumentType.profile())
                              .requires(iSender -> iSender.hasPermission("command.RSLB.profile.set.oneself")))
                           .executes(this::executeSetOneself)
                     ))
                  .then(
                     this.handler
                        .argument("profile", ProfileArgumentType.profile())
                        .then(
                           ((RequiredArgumentBuilder)this.handler
                                 .argument("online", OnlineArgumentType.online())
                                 .requires(iSender -> iSender.hasPermission("command.RSLB.profile.set.other")))
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
                        .requires(iSender -> iSender.hasPermission("command.RSLB.profile.remove")))
                     .executes(this::executeRemove)
               )
         );
   }

   private int executeRemove(CommandContext<ISender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         String name = Optional.ofNullable(profile.getProfileName())
            .orElse(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_unnamed"));
         this.handler
            .getSecondaryConfirmationHandler()
            .submit(
               (ISender)context.getSource(),
               () -> {
                  CommandHandler.getCore().getSqlManager().getInGameProfileTable().remove(profile.getProfileUUID());
                  ((ISender)context.getSource())
                     .sendMessagePL(
                        CommandHandler.getCore()
                           .getLanguageHandler()
                           .getMessage("command_message_profile_remove_succeed", new Pair("name", name), new Pair("uuid", profile.getProfileUUID()))
                     );
                  IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(profile.getProfileUUID());
                  if (player != null) {
                     player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_kickmessage"));
                  }
               },
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage("command_message_profile_remove_desc", new Pair("name", name), new Pair("uuid", profile.getProfileUUID())),
               CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_remove_cq")
            );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeSetOther(CommandContext<ISender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         this.processSet(context, online.getOnlineUUID(), online.getOnlineName(), online.getBaseServiceConfig().getId(), profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeSetOneself(CommandContext<ISender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         Pair<GameProfile, Integer> pair = this.handler.requireDataCacheArgumentSelf(context);
         this.processSet(context, ((GameProfile)pair.getValue1()).getId(), ((GameProfile)pair.getValue1()).getName(), (Integer)pair.getValue2(), profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private void processSet(CommandContext<ISender> context, UUID from, String fromName, int serviceId, ProfileArgumentType.ProfileArgument to) {
      this.handler
         .getSecondaryConfirmationHandler()
         .submit(
            (ISender)context.getSource(),
            () -> {
               CommandHandler.getCore().getSqlManager().getUserDataTable().setInGameUUID(from, serviceId, to.getProfileUUID());
               ((ISender)context.getSource())
                  .sendMessagePL(
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
                  CommandHandler.getCore()
                     .getPlugin()
                     .getRunServer()
                     .getPlayerManager()
                     .kickPlayerIfOnline(
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

   private void processCreate(CommandContext<ISender> context, String name, UUID uuid) throws SQLException {
      RSLVCore core = CommandHandler.getCore();
      String nameAllowedRegular = core.getPluginConfig().getNameAllowedRegular();
      if (!ValueUtil.isEmpty(nameAllowedRegular) && !Pattern.matches(nameAllowedRegular, name)) {
         ((ISender)context.getSource())
            .sendMessagePL(
               core.getLanguageHandler()
                  .getMessage("command_message_profile_create_namemismatch", new Pair("name", name), new Pair("regular", nameAllowedRegular))
            );
      } else if (uuid.version() < 2) {
         ((ISender)context.getSource())
            .sendMessagePL(core.getLanguageHandler().getMessage("command_message_profile_create_uuidmismatch", new Pair("uuid", uuid)));
      } else {
         Pair<UUID, String> pair = core.getSqlManager().getInGameProfileTable().get(uuid);
         if (pair != null) {
            ((ISender)context.getSource())
               .sendMessagePL(
                  core.getLanguageHandler()
                     .getMessage("command_message_profile_create_uuidoccupied", new Pair("uuid", uuid), new Pair("name", (String)pair.getValue2()))
               );
         } else {
            UUID uuidIgnoreCase = core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(name);
            if (uuidIgnoreCase != null) {
               ((ISender)context.getSource())
                  .sendMessagePL(
                     core.getLanguageHandler()
                        .getMessage("command_message_profile_create_nameoccupied", new Pair("name", name), new Pair("uuid", uuidIgnoreCase))
                  );
            } else {
               core.getSqlManager().getInGameProfileTable().insertNewData(uuid, name);
               ((ISender)context.getSource())
                  .sendMessagePL(core.getLanguageHandler().getMessage("command_message_profile_create", new Pair("uuid", name), new Pair("name", uuid)));
            }
         }
      }
   }

   private int executeCreate(CommandContext<ISender> context) {
      try {
         String username = StringArgumentType.getString(context, "username");
         UUID ingameuuid = UUIDArgumentType.getUuid(context, "ingameuuid");
         this.processCreate(context, username, ingameuuid);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeCreateRandomUUID(CommandContext<ISender> context) {
      try {
         String username = StringArgumentType.getString(context, "username");
         UUID ingameuuid = UUID.randomUUID();
         this.processCreate(context, username, ingameuuid);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
