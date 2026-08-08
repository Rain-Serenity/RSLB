package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.There;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.OnlineArgumentType;
import com.rserene.chosen.server.core.command.argument.ProfileArgumentType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public class MFindCommand {
   private final CommandHandler handler;

   public MFindCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)literalArgumentBuilder
            .requires(iSender ->
               iSender.hasPermission("rslb.find.online")
               || iSender.hasPermission("rslb.find.profile"))
            .then(
            ((LiteralArgumentBuilder)this.handler.literal("profile").requires(iSender -> iSender.hasPermission("rslb.find.profile")))
               .then(this.handler.argument("profile", ProfileArgumentType.profile()).executes(this::executeProfile))
         ))
         .then(
            ((LiteralArgumentBuilder)this.handler.literal("online").requires(iSender -> iSender.hasPermission("rslb.find.online")))
               .then(this.handler.argument("online", OnlineArgumentType.online()).executes(this::executeOnline))
         );
   }

   private int executeOnline(CommandContext<ISender> context) {
      try {
         OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
         String whitelist = online.isWhitelist()
            ? CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_whitelist_true")
            : CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_whitelist_false");
         UUID profileUUID = online.getProfileUUID();
         if (profileUUID == null) {
            ((ISender)context.getSource())
               .sendMessagePL(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_find_online",
                        new Pair("service_name", online.getBaseServiceConfig().getName()),
                        new Pair("service_id", online.getBaseServiceConfig().getId()),
                        new Pair("online_uuid", online.getOnlineUUID()),
                        new Pair("online_name", online.getOnlineName()),
                        new Pair("whitelist", whitelist),
                        new Pair("profile", CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_profilenotexist"))
                     )
               );
            return 0;
         }

         String profileName = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getUsername(profileUUID);
         if (profileName == null) {
            profileName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_online_profileunnamed");
         }

         String profileInfo = CommandHandler.getCore()
            .getLanguageHandler()
            .getMessage("command_message_find_online_profile", new Pair("profile_uuid", profileUUID), new Pair("profile_name", profileName));
         ((ISender)context.getSource())
            .sendMessagePL(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_find_online",
                     new Pair("service_name", online.getBaseServiceConfig().getName()),
                     new Pair("service_id", online.getBaseServiceConfig().getId()),
                     new Pair("online_uuid", online.getOnlineUUID()),
                     new Pair("online_name", online.getOnlineName()),
                     new Pair("whitelist", whitelist),
                     new Pair("profile", profileInfo)
                  )
            );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeProfile(CommandContext<ISender> context) {
      try {
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         UUID profileUUID = profile.getProfileUUID();
         Set<There<UUID, String, Integer>> onlineProfiles = CommandHandler.getCore().getSqlManager().getUserDataTable().getOnlineProfiles(profileUUID);
         String profileName = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getUsername(profileUUID);
         if (profileName == null) {
            profileName = CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_unnamed");
         }

         String message = CommandHandler.getCore()
            .getLanguageHandler()
            .getMessage(
               "command_message_find_profile",
               new Pair("profile_uuid", profileUUID),
               new Pair("profile_name", profileName),
               new Pair("count", onlineProfiles.size()),
               new Pair(
                  "list",
                  onlineProfiles.stream()
                     .map(
                        p -> {
                           BaseServiceConfig serviceConfig = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(p.getValue3());
                           String serviceName = serviceConfig == null
                              ? CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_unused_service")
                              : serviceConfig.getName();
                           return CommandHandler.getCore()
                              .getLanguageHandler()
                              .getMessage(
                                 "command_message_find_profile_entry",
                                 new Pair("service_name", serviceName),
                                 new Pair("service_id", (Integer)p.getValue3()),
                                 new Pair("online_uuid", (UUID)p.getValue1()),
                                 new Pair(
                                    "online_name",
                                    (String)Optional.ofNullable((String)p.getValue2())
                                       .orElse(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_onlineunnamed"))
                                 )
                              );
                        }
                     )
                     .collect(Collectors.joining(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_delimiter")))
               )
            );
         ((ISender)context.getSource()).sendMessagePL(message);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }
}
