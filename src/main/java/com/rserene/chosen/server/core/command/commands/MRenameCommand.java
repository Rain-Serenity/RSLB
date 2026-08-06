package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.argument.ProfileArgumentType;
import com.rserene.chosen.server.core.command.argument.StringArgumentType;

public class MRenameCommand {
   private final CommandHandler handler;

   public MRenameCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)((LiteralArgumentBuilder)literalArgumentBuilder
            .requires(iSender ->
               iSender.hasPermission("RSLB.rename.oneself")
               || iSender.hasPermission("RSLB.rename.other"))
            .then(
            ((RequiredArgumentBuilder)this.handler
                  .argument("newname", StringArgumentType.string())
                  .requires(iSender -> iSender.hasPermission("RSLB.rename.oneself")))
               .executes(this::executeRename)
         ))
         .then(
            this.handler
               .argument("newname", StringArgumentType.string())
               .then(
                  ((RequiredArgumentBuilder)this.handler
                        .argument("profile", ProfileArgumentType.profile())
                        .requires(iSender -> iSender.hasPermission("RSLB.rename.other")))
                     .executes(this::executeRenameOther)
               )
         );
   }

   private int executeRenameOther(CommandContext<ISender> context) {
      try {
         String newname = StringArgumentType.getString(context, "newname");
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         this.processRename(context, newname, profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeRename(CommandContext<ISender> context) {
      try {
         String newname = StringArgumentType.getString(context, "newname");
         this.handler.requireDataCacheArgumentSelf(context);
         this.processRename(
            context,
            newname,
            new ProfileArgumentType.ProfileArgument(
               ((ISender)context.getSource()).getAsPlayer().getUniqueId(), ((ISender)context.getSource()).getAsPlayer().getName()
            )
         );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private void processRename(CommandContext<ISender> context, String newName, ProfileArgumentType.ProfileArgument argument) {
      if (newName.equals(argument.getProfileName())) {
         ((ISender)context.getSource()).sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_identical"));
      } else {
         String nameAllowedRegular = CommandHandler.getCore().getPluginConfig().getNameAllowedRegular();
         if (!ValueUtil.isEmpty(nameAllowedRegular) && !Pattern.matches(nameAllowedRegular, newName)) {
            ((ISender)context.getSource())
               .sendMessagePL(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage("command_message_rename_mismatch", new Pair("name", newName), new Pair("regular", nameAllowedRegular))
               );
         } else {
            this.handler
               .getSecondaryConfirmationHandler()
               .submit(
                  (ISender)context.getSource(),
                  () -> {
                     try {
                        CommandHandler.getCore().getSqlManager().getInGameProfileTable().updateUsername(argument.getProfileUUID(), newName);
                        ((ISender)context.getSource())
                           .sendMessagePL(
                              CommandHandler.getCore()
                                 .getLanguageHandler()
                                 .getMessage(
                                    "command_message_rename_succeed",
                                    new Pair("profile_name", argument.getProfileName()),
                                    new Pair("new_name", newName),
                                    new Pair("profile_uuid", argument.getProfileUUID())
                                 )
                           );
                        CommandHandler.getCore()
                           .getPlugin()
                           .getRunServer()
                           .getPlayerManager()
                           .kickPlayerIfOnline(
                              argument.getProfileUUID(),
                              CommandHandler.getCore()
                                 .getLanguageHandler()
                                 .getMessage(
                                    "command_message_rename_succeed_kickmessage",
                                    new Pair("profile_name", argument.getProfileName()),
                                    new Pair("new_name", newName),
                                    new Pair("profile_uuid", argument.getProfileUUID())
                                 )
                           );
                     } catch (SQLIntegrityConstraintViolationException e) {
                        ((ISender)context.getSource())
                           .sendMessagePL(
                              CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_occupied", new Pair("name", newName))
                           );
                     }
                  },
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_rename_desc",
                        new Pair("profile_name", argument.getProfileName()),
                        new Pair("new_name", newName),
                        new Pair("profile_uuid", argument.getProfileUUID())
                     ),
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_rename_cq",
                        new Pair("profile_name", argument.getProfileName()),
                        new Pair("new_name", newName),
                        new Pair("profile_uuid", argument.getProfileUUID())
                     )
               );
         }
      }
   }
}
