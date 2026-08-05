package com.rserene.chosen.server.core.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public class StringArgumentType implements ArgumentType<String> {
   public static StringArgumentType string() {
      return new StringArgumentType();
   }

   public static String getString(CommandContext<?> context, String name) {
      return (String)context.getArgument(name, String.class);
   }

   public static String readString(StringReader reader) {
      int argBeginning = reader.getCursor();

      while (reader.canRead() && reader.peek() != ' ') {
         reader.skip();
      }

      return reader.getString().substring(argBeginning, reader.getCursor());
   }

   public String parse(StringReader reader) {
      return readString(reader);
   }
}
