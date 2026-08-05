package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.command.UniversalCommandExceptionType;

public class UUIDArgumentType implements ArgumentType<UUID> {
   public static UUIDArgumentType uuid() {
      return new UUIDArgumentType();
   }

   public static UUID getUuid(CommandContext<?> context, String name) {
      return (UUID)context.getArgument(name, UUID.class);
   }

   public UUID parse(StringReader reader) throws CommandSyntaxException {
      int argBeginning = reader.getCursor();
      String uuidString = StringArgumentType.readString(reader);
      UUID ret = ValueUtil.getUuidOrNull(uuidString);
      if (ret == null) {
         reader.setCursor(argBeginning);
         throw UniversalCommandExceptionType.create(
            CommandHandler.getCore().getLanguageHandler().getMessage("command_exception_reader_invalid_uuid", new Pair("value", uuidString)), reader
         );
      } else {
         return ret;
      }
   }

   @Generated
   private UUIDArgumentType() {
   }
}
