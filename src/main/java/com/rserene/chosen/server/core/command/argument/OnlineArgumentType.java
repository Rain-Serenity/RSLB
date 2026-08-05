package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.There;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.UniversalCommandExceptionType;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.database.table.UserDataTableV3;

public class OnlineArgumentType implements ArgumentType<OnlineArgumentType.OnlineArgument> {
   public static OnlineArgumentType online() {
      return new OnlineArgumentType();
   }

   public static OnlineArgumentType.OnlineArgument getOnline(CommandContext<?> context, String name) {
      return (OnlineArgumentType.OnlineArgument)context.getArgument(name, OnlineArgumentType.OnlineArgument.class);
   }

   public OnlineArgumentType.OnlineArgument parse(StringReader reader) throws CommandSyntaxException {
      try {
         int i = reader.getCursor();
         BaseServiceConfig serviceConfig = ServiceIdArgumentType.readServiceConfig(reader);
         if (!reader.canRead()) {
            reader.setCursor(i);
            throw CommandHandler.getBuiltInExceptions().dispatcherUnknownCommand().createWithContext(reader);
         }

         reader.skip();
         String nameOrUuid = StringArgumentType.readString(reader);
         UserDataTableV3 dataTable = CommandHandler.getCore().getSqlManager().getUserDataTable();
         UUID uuid = ValueUtil.getUuidOrNull(nameOrUuid);
         if (uuid == null) {
            uuid = dataTable.getOnlineUUID(nameOrUuid, serviceConfig.getId());
            if (uuid == null) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore()
                     .getLanguageHandler()
                     .getMessage(
                        "command_message_online_not_found_by_name",
                        new Pair("service_name", serviceConfig.getName()),
                        new Pair("service_id", serviceConfig.getId()),
                        new Pair("online_name", nameOrUuid)
                     ),
                  reader
               );
            }
         }

         There<String, UUID, Boolean> there = dataTable.get(uuid, serviceConfig.getId());
         if (there == null) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
               CommandHandler.getCore()
                  .getLanguageHandler()
                  .getMessage(
                     "command_message_online_not_found_by_uuid",
                     new Pair("service_name", serviceConfig.getName()),
                     new Pair("service_id", serviceConfig.getId()),
                     new Pair("online_uuid", uuid)
                  ),
               reader
            );
         } else {
            return new OnlineArgumentType.OnlineArgument(serviceConfig, uuid, (String)there.getValue1(), (UUID)there.getValue2(), (Boolean)there.getValue3());
         }
      } catch (Throwable $ex) {
         throw com.rserene.chosen.server.api.internal.util.ValueUtil.sneakyThrow($ex);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return ServiceIdArgumentType.getSuggestions(context, builder);
   }

   public static class OnlineArgument {
      private final BaseServiceConfig baseServiceConfig;
      private final UUID onlineUUID;
      private final String onlineName;
      private final UUID profileUUID;
      private final boolean whitelist;

      @Generated
      private OnlineArgument(BaseServiceConfig baseServiceConfig, UUID onlineUUID, String onlineName, UUID profileUUID, boolean whitelist) {
         this.baseServiceConfig = baseServiceConfig;
         this.onlineUUID = onlineUUID;
         this.onlineName = onlineName;
         this.profileUUID = profileUUID;
         this.whitelist = whitelist;
      }

      @Generated
      public BaseServiceConfig getBaseServiceConfig() {
         return this.baseServiceConfig;
      }

      @Generated
      public UUID getOnlineUUID() {
         return this.onlineUUID;
      }

      @Generated
      public String getOnlineName() {
         return this.onlineName;
      }

      @Generated
      public UUID getProfileUUID() {
         return this.profileUUID;
      }

      @Generated
      public boolean isWhitelist() {
         return this.whitelist;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof OnlineArgumentType.OnlineArgument other)) {
            return false;
         } else {
            if (!other.canEqual(this)) {
               return false;
            }

            if (this.isWhitelist() != other.isWhitelist()) {
               return false;
            }

            Object this$baseServiceConfig = this.getBaseServiceConfig();
            Object other$baseServiceConfig = other.getBaseServiceConfig();
            if (this$baseServiceConfig == null ? other$baseServiceConfig == null : this$baseServiceConfig.equals(other$baseServiceConfig)) {
               Object this$onlineUUID = this.getOnlineUUID();
               Object other$onlineUUID = other.getOnlineUUID();
               if (this$onlineUUID == null ? other$onlineUUID == null : this$onlineUUID.equals(other$onlineUUID)) {
                  Object this$onlineName = this.getOnlineName();
                  Object other$onlineName = other.getOnlineName();
                  if (this$onlineName == null ? other$onlineName == null : this$onlineName.equals(other$onlineName)) {
                     Object this$profileUUID = this.getProfileUUID();
                     Object other$profileUUID = other.getProfileUUID();
                     return this$profileUUID == null ? other$profileUUID == null : this$profileUUID.equals(other$profileUUID);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof OnlineArgumentType.OnlineArgument;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + (this.isWhitelist() ? 79 : 97);
         Object $baseServiceConfig = this.getBaseServiceConfig();
         result = result * 59 + ($baseServiceConfig == null ? 43 : $baseServiceConfig.hashCode());
         Object $onlineUUID = this.getOnlineUUID();
         result = result * 59 + ($onlineUUID == null ? 43 : $onlineUUID.hashCode());
         Object $onlineName = this.getOnlineName();
         result = result * 59 + ($onlineName == null ? 43 : $onlineName.hashCode());
         Object $profileUUID = this.getProfileUUID();
         return result * 59 + ($profileUUID == null ? 43 : $profileUUID.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "OnlineArgumentType.OnlineArgument(baseServiceConfig="
            + this.getBaseServiceConfig()
            + ", onlineUUID="
            + this.getOnlineUUID()
            + ", onlineName="
            + this.getOnlineName()
            + ", profileUUID="
            + this.getProfileUUID()
            + ", whitelist="
            + this.isWhitelist()
            + ")";
      }
   }
}
