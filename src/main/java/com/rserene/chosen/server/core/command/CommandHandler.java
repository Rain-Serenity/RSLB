package com.rserene.chosen.server.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.command.CommandAPI;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.command.commands.RootCommand;
import com.rserene.chosen.server.core.main.RSLBCore;

public class CommandHandler implements CommandAPI {
   private static RSLBCore core;
   private static BuiltInExceptions builtInExceptions;
   private final CommandDispatcher<ISender> dispatcher;
   private final SecondaryConfirmationHandler secondaryConfirmationHandler;

   public CommandHandler(RSLBCore core) {
      CommandHandler.core = core;
      this.dispatcher = new CommandDispatcher();
      this.secondaryConfirmationHandler = new SecondaryConfirmationHandler();
   }

   public void init() {
      this.dispatcher.register(new RootCommand(this).register(this.literal("RSLB")));
      this.dispatcher.register(new RootCommand(this).register(this.literal("rsl")));
      CommandSyntaxException.BUILT_IN_EXCEPTIONS = builtInExceptions = new BuiltInExceptions(core);
   }

   public void execute(ISender sender, String[] args) {
      this.execute(sender, String.join(" ", args));
   }

   public void execute(ISender sender, String args) {
      core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
         try {
            this.dispatcher.execute(args, sender);
         } catch (CommandSyntaxException e) {
            sender.sendMessagePL(e.getRawMessage().getString());
            LoggerProvider.getLogger().debug(String.format("An expected exception occurs when the %s command is executed.", String.join(" ", args)), e);
         } catch (Exception e) {
            sender.sendMessagePL(core.getLanguageHandler().getMessage("command_error"));
            LoggerProvider.getLogger().error(String.format("An exception occurs when the %s command is executed.", String.join(" ", args)), e);
         }
      });
   }

   public List<String> tabComplete(ISender sender, String[] args) {
      return args.length == 1 ? this.tabComplete(sender, args[0] + " ") : this.tabComplete(sender, String.join(" ", args));
   }

   public List<String> tabComplete(ISender sender, String args) {
      if (!sender.hasPermission("RSLB.tab.complete")) {
         return Collections.emptyList();
      }

      CompletableFuture<Suggestions> suggestions = this.dispatcher.getCompletionSuggestions(this.dispatcher.parse(args, sender));
      List<String> ret = new ArrayList<>();

      try {
         Suggestions suggestions1 = suggestions.get(2L, java.util.concurrent.TimeUnit.SECONDS);

         for (Suggestion suggestion : suggestions1.getList()) {
            String text = suggestion.getText().trim();
            if (!text.isEmpty()) {
               ret.add(text);
            }
         }
      } catch (Exception e) {
         LoggerProvider.getLogger().debug(String.format("An exception occurred while executing the %s command to complete.", String.join(" ", args)), e);
      }

      return ret;
   }

   public final LiteralArgumentBuilder<ISender> literal(String literal) {
      return LiteralArgumentBuilder.literal(literal);
   }

   public final <T> RequiredArgumentBuilder<ISender, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   public final void requirePlayer(CommandContext<ISender> context) throws CommandSyntaxException {
      if (!((ISender)context.getSource()).isPlayer()) {
         throw builtInExceptions.requirePlayer().create();
      }
   }

   public final void requirePlayerAndNoSelf(CommandContext<ISender> context, IPlayer player) throws CommandSyntaxException {
      if (!((ISender)context.getSource()).isPlayer()) {
         throw builtInExceptions.requirePlayer().create();
      }

      if (((ISender)context.getSource()).getAsPlayer().getUniqueId().equals(player.getUniqueId())) {
         throw builtInExceptions.noSelf().create();
      }
   }

   public final Pair<GameProfile, Integer> requireDataCacheArgumentSelf(CommandContext<ISender> context) throws CommandSyntaxException {
      this.requirePlayer(context);
      Pair<GameProfile, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(((ISender)context.getSource()).getAsPlayer().getUniqueId());
      if (profile == null) {
         throw builtInExceptions.cacheNotFoundSelf().create();
      } else {
         return profile;
      }
   }

   public final Pair<GameProfile, Integer> requireDataCacheArgumentOther(IPlayer player) throws CommandSyntaxException {
      Pair<GameProfile, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(player.getUniqueId());
      if (profile == null) {
         throw builtInExceptions.cacheNotFoundOther().create(player.getUniqueId(), player.getName());
      } else {
         return profile;
      }
   }

   @Generated
   public static RSLBCore getCore() {
      return core;
   }

   @Generated
   public static BuiltInExceptions getBuiltInExceptions() {
      return builtInExceptions;
   }

   @Generated
   public SecondaryConfirmationHandler getSecondaryConfirmationHandler() {
      return this.secondaryConfirmationHandler;
   }
}
