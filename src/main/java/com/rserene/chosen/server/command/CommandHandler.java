package com.rserene.chosen.server.command;

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
import java.util.logging.Level;
import lombok.Generated;
import com.rserene.chosen.server.command.CommandAPI;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.command.commands.RootCommand;
import com.rserene.chosen.server.main.RSLBCore;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandAPI {
   private static RSLBCore core;
   private static BuiltInExceptions builtInExceptions;
   private final CommandDispatcher<CommandSender> dispatcher;
   private final SecondaryConfirmationHandler secondaryConfirmationHandler;

   public CommandHandler(RSLBCore core) {
      CommandHandler.core = core;
      this.dispatcher = new CommandDispatcher();
      this.secondaryConfirmationHandler = new SecondaryConfirmationHandler();
   }

   public void init() {
      this.dispatcher.register(new RootCommand(this).register(this.literal("rslb")));
      builtInExceptions = new BuiltInExceptions(core);
   }

   public void execute(CommandSender sender, String[] args) {
      this.execute(sender, String.join(" ", args));
   }

   public void execute(CommandSender sender, String args) {
      Bukkit.getAsyncScheduler().runNow(core.getPlugin(), task -> {
         try {
            this.dispatcher.execute(args, sender);
         } catch (CommandSyntaxException e) {
            MessageUtil.sendLegacy(sender, e.getRawMessage().getString());
            core.logDebug(String.format("An expected exception occurs when the %s command is executed.", String.join(" ", args)), e);
         } catch (Exception e) {
            MessageUtil.sendLegacy(sender, core.getLanguageHandler().getMessage("command_error"));
            core.getLogger().log(Level.SEVERE, String.format("An exception occurs when the %s command is executed.", String.join(" ", args)), e);
         }
      });
   }

   public List<String> tabComplete(CommandSender sender, String[] args) {
      return args.length == 1 ? this.tabComplete(sender, args[0] + " ") : this.tabComplete(sender, String.join(" ", args));
   }

   public List<String> tabComplete(CommandSender sender, String args) {
      if (!sender.hasPermission("rslb.tab.complete")) {
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
         core.logDebug(String.format("An exception occurred while executing the %s command to complete.", String.join(" ", args)), e);
      }

      return ret;
   }

   public final LiteralArgumentBuilder<CommandSender> literal(String literal) {
      return LiteralArgumentBuilder.literal(literal);
   }

   public final <T> RequiredArgumentBuilder<CommandSender, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   public final void requirePlayer(CommandContext<CommandSender> context) throws CommandSyntaxException {
      if (!(context.getSource() instanceof Player)) {
         throw builtInExceptions.requirePlayer().create();
      }
   }

   public final void requirePlayerAndNoSelf(CommandContext<CommandSender> context, Player player) throws CommandSyntaxException {
      if (!(context.getSource() instanceof Player)) {
         throw builtInExceptions.requirePlayer().create();
      }

      if (((Player)context.getSource()).getUniqueId().equals(player.getUniqueId())) {
         throw builtInExceptions.noSelf().create();
      }
   }

   public final Pair<GameProfile, Integer> requireDataCacheArgumentSelf(CommandContext<CommandSender> context) throws CommandSyntaxException {
      this.requirePlayer(context);
      Pair<GameProfile, Integer> profile = core.getPlayerHandler().getPlayerOnlineProfile(((Player)context.getSource()).getUniqueId());
      if (profile == null) {
         throw builtInExceptions.cacheNotFoundSelf().create();
      } else {
         return profile;
      }
   }

   public final Pair<GameProfile, Integer> requireDataCacheArgumentOther(Player player) throws CommandSyntaxException {
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