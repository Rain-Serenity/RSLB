package com.rserene.chosen.server.command.argument;

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
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.util.ValueUtil;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.command.UniversalCommandExceptionType;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OnlinePlayerArgumentType implements ArgumentType<Set<Player>> {
   public static OnlinePlayerArgumentType players() {
      return new OnlinePlayerArgumentType();
   }

   public static Set<Player> getPlayers(CommandContext<?> context, String name) {
      return (Set<Player>)context.getArgument(name, Set.class);
   }

   public static Player getPlayer(CommandContext<?> context, String name) throws CommandSyntaxException {
      Set<Player> players = getPlayers(context, name);
      if (players.size() == 1) {
         return players.iterator().next();
      } else {
         throw UniversalCommandExceptionType.create(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_multi_target"));
      }
   }

   public Set<Player> parse(StringReader reader) {
      try {
         int i = reader.getCursor();
         String string = StringArgumentType.readString(reader);
         UUID uuidOrNull = ValueUtil.getUuidOrNull(string);
         if (uuidOrNull != null) {
            Player player = Bukkit.getPlayer(uuidOrNull);
            if (player == null) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_player_not_online_by_uuid", new Pair("uuid", string)), reader
               );
            } else {
               HashSet<Player> players = new HashSet<>();
               players.add(player);
               return players;
            }
         } else {
            Set<Player> players = MessageUtil.getPlayers(string);
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
         throw com.rserene.chosen.server.util.ValueUtil.sneakyThrow($ex);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      for (Player key : MessageUtil.getOnlinePlayers()) {
         if (key.getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(key.getName());
         }
      }

      return builder.buildFuture();
   }
}