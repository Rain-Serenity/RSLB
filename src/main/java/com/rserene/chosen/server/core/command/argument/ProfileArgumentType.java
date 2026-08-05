package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.UniversalCommandExceptionType;
import com.rserene.chosen.server.core.database.table.InGameProfileTableV3;

public class ProfileArgumentType implements ArgumentType<ProfileArgumentType.ProfileArgument> {
   public static ProfileArgumentType profile() {
      return new ProfileArgumentType();
   }

   public static ProfileArgumentType.ProfileArgument getProfile(CommandContext<?> context, String name) {
      return (ProfileArgumentType.ProfileArgument)context.getArgument(name, ProfileArgumentType.ProfileArgument.class);
   }

   public ProfileArgumentType.ProfileArgument parse(StringReader reader) {
      try {
         int i = reader.getCursor();
         String nameOrUuid = StringArgumentType.readString(reader);
         InGameProfileTableV3 table = CommandHandler.getCore().getSqlManager().getInGameProfileTable();
         UUID uuid = ValueUtil.getUuidOrNull(nameOrUuid);
         if (uuid == null) {
            uuid = table.getInGameUUIDIgnoreCase(nameOrUuid);
            if (uuid == null) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_not_found_by_name", new Pair("name", nameOrUuid)), reader
               );
            } else {
               return new ProfileArgumentType.ProfileArgument(uuid, table.getUsername(uuid));
            }
         } else {
            String username = table.getUsername(uuid);
            if (username == null) {
               reader.setCursor(i);
               throw UniversalCommandExceptionType.create(
                  CommandHandler.getCore().getLanguageHandler().getMessage("command_message_profile_not_found_by_uuid", new Pair("uuid", uuid)), reader
               );
            } else {
               return new ProfileArgumentType.ProfileArgument(uuid, username);
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

   public static class ProfileArgument {
      private final UUID profileUUID;
      private final String profileName;

      @Generated
      public ProfileArgument(UUID profileUUID, String profileName) {
         this.profileUUID = profileUUID;
         this.profileName = profileName;
      }

      @Generated
      public UUID getProfileUUID() {
         return this.profileUUID;
      }

      @Generated
      public String getProfileName() {
         return this.profileName;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ProfileArgumentType.ProfileArgument other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$profileUUID = this.getProfileUUID();
            Object other$profileUUID = other.getProfileUUID();
            if (this$profileUUID == null ? other$profileUUID == null : this$profileUUID.equals(other$profileUUID)) {
               Object this$profileName = this.getProfileName();
               Object other$profileName = other.getProfileName();
               return this$profileName == null ? other$profileName == null : this$profileName.equals(other$profileName);
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ProfileArgumentType.ProfileArgument;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $profileUUID = this.getProfileUUID();
         result = result * 59 + ($profileUUID == null ? 43 : $profileUUID.hashCode());
         Object $profileName = this.getProfileName();
         return result * 59 + ($profileName == null ? 43 : $profileName.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "ProfileArgumentType.ProfileArgument(profileUUID=" + this.getProfileUUID() + ", profileName=" + this.getProfileName() + ")";
      }
   }
}
