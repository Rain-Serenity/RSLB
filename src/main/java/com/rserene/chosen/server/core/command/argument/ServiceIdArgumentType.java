package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.UniversalCommandExceptionType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;

public class ServiceIdArgumentType implements ArgumentType<BaseServiceConfig> {
   public static ServiceIdArgumentType service() {
      return new ServiceIdArgumentType();
   }

   public static BaseServiceConfig getService(CommandContext<?> context, String name) {
      return (BaseServiceConfig)context.getArgument(name, BaseServiceConfig.class);
   }

   protected static BaseServiceConfig readServiceConfig(StringReader reader) throws CommandSyntaxException {
      int start = reader.getCursor();
      int result = reader.readInt();
      BaseServiceConfig config = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(result);
      if (config == null) {
         reader.setCursor(start);
         throw UniversalCommandExceptionType.create(
            CommandHandler.getCore().getLanguageHandler().getMessage("command_exception_serviceid_not_found", new Pair("service_id", result)), reader
         );
      } else {
         return config;
      }
   }

   public static <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      CommandHandler.getCore().getPluginConfig().getServiceIdMap().forEach((key, value) -> {
         if ((key + "").startsWith(builder.getRemaining().toLowerCase())) {
            builder.suggest(key);
         }
      });
      return builder.buildFuture();
   }

   public BaseServiceConfig parse(StringReader reader) throws CommandSyntaxException {
      return readServiceConfig(reader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return getSuggestions(context, builder);
   }

   @Generated
   private ServiceIdArgumentType() {
   }
}
