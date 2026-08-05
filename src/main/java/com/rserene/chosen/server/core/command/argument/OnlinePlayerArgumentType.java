package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.UniversalCommandExceptionType;

public class OnlinePlayerArgumentType implements ArgumentType<Set<IPlayer>> {
   public static OnlinePlayerArgumentType players() {
      return new OnlinePlayerArgumentType();
   }

   public static Set<IPlayer> getPlayers(CommandContext<?> context, String name) {
      return (Set<IPlayer>)context.getArgument(name, Set.class);
   }

   public static IPlayer getPlayer(CommandContext<?> context, String name) throws CommandSyntaxException {
      Set<IPlayer> players = getPlayers(context, name);
      if (players.size() == 1) {
         return players.iterator().next();
      } else {
         throw UniversalCommandExceptionType.create(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_multi_target"));
      }
   }

   public Set<IPlayer> parse(StringReader reader) {
      try {
         int i = reader.getCursor();
         String string = StringArgumentType.readString(reader);
         UUID uuidOrNull = ValueUtil.getUuidOrNull(string);
         if (uuidOrNull != null) {
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(uuidOrNull);
            if (player == null) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_uuid", new Pair("uuid", string)), reader
               );
            } else {
               HashSet<IPlayer> players = new HashSet<>();
               players.add(player);
               return players;
            }
         } else {
            Set<IPlayer> players = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayers(string);
            if (players.isEmpty()) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_name", new Pair("name", string)), reader
               );
            } else {
               return players;
            }
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      for (IPlayer key : CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers()) {
         if (key.getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(key.getName());
         }
      }

      return builder.buildFuture();
   }
}
