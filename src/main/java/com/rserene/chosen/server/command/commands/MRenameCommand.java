package com.rserene.chosen.server.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.util.ValueUtil;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.argument.ProfileArgumentType;
import com.rserene.chosen.server.command.argument.StringArgumentType;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MRenameCommand {
   private final CommandHandler handler;

   public MRenameCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<CommandSender>)((LiteralArgumentBuilder)literalArgumentBuilder
            .requires(iSender ->
               iSender.hasPermission("rslb.rename.oneself")
               || iSender.hasPermission("rslb.rename.other"))
            .then(
            ((RequiredArgumentBuilder)this.handler
                  .argument("newname", StringArgumentType.string())
                  .requires(iSender -> iSender.hasPermission("rslb.rename.oneself")))
               .executes(this::executeRename)
         ))
         .then(
            this.handler
               .argument("newname", StringArgumentType.string())
               .then(
                  ((RequiredArgumentBuilder)this.handler
                        .argument("profile", ProfileArgumentType.profile())
                        .requires(iSender -> iSender.hasPermission("rslb.rename.other")))
                     .executes(this::executeRenameOther)
               )
         );
   }

   private int executeRenameOther(CommandContext<CommandSender> context) {
      try {
         String newname = StringArgumentType.getString(context, "newname");
         ProfileArgumentType.ProfileArgument profile = ProfileArgumentType.getProfile(context, "profile");
         this.processRename(context, newname, profile);
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private int executeRename(CommandContext<CommandSender> context) {
      try {
         String newname = StringArgumentType.getString(context, "newname");
         this.handler.requireDataCacheArgumentSelf(context);
         this.processRename(
            context,
            newname,
            new ProfileArgumentType.ProfileArgument(
               ((Player)context.getSource()).getUniqueId(), ((Player)context.getSource()).getName()
            )
         );
         return 0;
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   private void processRename(CommandContext<CommandSender> context, String newName, ProfileArgumentType.ProfileArgument argument) {
      if (newName.equals(argument.getProfileName())) {
         MessageUtil.sendLegacy(context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_identical"));
      } else {
         String nameAllowedRegular = CommandHandler.getCore().getPluginConfig().getNameAllowedRegular();
         if (!ValueUtil.isEmpty(nameAllowedRegular) && !Pattern.matches(nameAllowedRegular, newName)) {
            MessageUtil.sendLegacy(
               context.getSource(),
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage("command_message_rename_mismatch", new Pair("name", newName), new Pair("regular", nameAllowedRegular))
            );
         } else {
            this.handler
               .getSecondaryConfirmationHandler()
               .submit(
                  context.getSource(),
                  () -> {
                     try {
                        CommandHandler.getCore().getSqlManager().getInGameProfileTable().updateUsername(argument.getProfileUUID(), newName);
                        MessageUtil.sendLegacy(
                           context.getSource(),
                           CommandHandler.getCore()
                              .getLanguageHandler()
                              .getMessage(
                                 "command_message_rename_succeed",
                                 new Pair("profile_name", argument.getProfileName()),
                                 new Pair("new_name", newName),
                                 new Pair("profile_uuid", argument.getProfileUUID())
                              )
                        );
                        MessageUtil.kickPlayerIfOnline(
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
                        MessageUtil.sendLegacy(
                           context.getSource(), CommandHandler.getCore().getLanguageHandler().getMessage("command_message_rename_occupied", new Pair("name", newName))
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