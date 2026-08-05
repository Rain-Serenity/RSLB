package com.rserene.chosen.server.core.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.core.main.RSLVCore;

public class BuiltInExceptions implements BuiltInExceptionProvider {
   private final Dynamic2CommandExceptionType DOUBLE_TOO_SMALL;
   private final Dynamic2CommandExceptionType DOUBLE_TOO_BIG;
   private final Dynamic2CommandExceptionType FLOAT_TOO_SMALL;
   private final Dynamic2CommandExceptionType FLOAT_TOO_BIG;
   private final Dynamic2CommandExceptionType INTEGER_TOO_SMALL;
   private final Dynamic2CommandExceptionType INTEGER_TOO_BIG;
   private final Dynamic2CommandExceptionType LONG_TOO_SMALL;
   private final Dynamic2CommandExceptionType LONG_TOO_BIG;
   private final DynamicCommandExceptionType LITERAL_INCORRECT;
   private final SimpleCommandExceptionType READER_EXPECTED_START_OF_QUOTE;
   private final SimpleCommandExceptionType READER_EXPECTED_END_OF_QUOTE;
   private final DynamicCommandExceptionType READER_INVALID_ESCAPE;
   private final DynamicCommandExceptionType READER_INVALID_BOOL;
   private final DynamicCommandExceptionType READER_INVALID_INT;
   private final SimpleCommandExceptionType READER_EXPECTED_INT;
   private final DynamicCommandExceptionType READER_INVALID_LONG;
   private final SimpleCommandExceptionType READER_EXPECTED_LONG;
   private final DynamicCommandExceptionType READER_INVALID_DOUBLE;
   private final SimpleCommandExceptionType READER_EXPECTED_DOUBLE;
   private final DynamicCommandExceptionType READER_INVALID_FLOAT;
   private final SimpleCommandExceptionType READER_EXPECTED_FLOAT;
   private final SimpleCommandExceptionType READER_EXPECTED_BOOL;
   private final DynamicCommandExceptionType READER_EXPECTED_SYMBOL;
   private final SimpleCommandExceptionType DISPATCHER_UNKNOWN_COMMAND;
   private final SimpleCommandExceptionType DISPATCHER_UNKNOWN_ARGUMENT;
   private final SimpleCommandExceptionType DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
   private final DynamicCommandExceptionType DISPATCHER_PARSE_EXCEPTION;
   private final SimpleCommandExceptionType REQUIRE_PLAYER;
   private final DynamicCommandExceptionType PLAYER_NOT_ONLINE;
   private final SimpleCommandExceptionType CACHE_NOT_FOUND_SELF;
   private final SimpleCommandExceptionType NO_SELF;
   private final Dynamic2CommandExceptionType CACHE_NOT_FOUND_OTHER;

   public BuiltInExceptions(RSLVCore core) {
      this.DOUBLE_TOO_SMALL = new Dynamic2CommandExceptionType(
         (found, min) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_double_too_small", new Pair("found", found), new Pair("min", min))
         )
      );
      this.DOUBLE_TOO_BIG = new Dynamic2CommandExceptionType(
         (found, max) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_double_too_big", new Pair("found", found), new Pair("max", max))
         )
      );
      this.FLOAT_TOO_SMALL = new Dynamic2CommandExceptionType(
         (found, min) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_float_too_small", new Pair("found", found), new Pair("min", min))
         )
      );
      this.FLOAT_TOO_BIG = new Dynamic2CommandExceptionType(
         (found, max) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_float_too_big", new Pair("found", found), new Pair("max", max))
         )
      );
      this.INTEGER_TOO_SMALL = new Dynamic2CommandExceptionType(
         (found, min) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_integer_too_small", new Pair("found", found), new Pair("min", min))
         )
      );
      this.INTEGER_TOO_BIG = new Dynamic2CommandExceptionType(
         (found, max) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_integer_too_big", new Pair("found", found), new Pair("max", max))
         )
      );
      this.LONG_TOO_SMALL = new Dynamic2CommandExceptionType(
         (found, min) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_long_too_small", new Pair("found", found), new Pair("min", min))
         )
      );
      this.LONG_TOO_BIG = new Dynamic2CommandExceptionType(
         (found, max) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_exception_long_too_big", new Pair("found", found), new Pair("max", max))
         )
      );
      this.LITERAL_INCORRECT = new DynamicCommandExceptionType(
         expected -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_literal_incorrect", new Pair("expected", expected)))
      );
      this.READER_EXPECTED_START_OF_QUOTE = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_start_of_quote"))
      );
      this.READER_EXPECTED_END_OF_QUOTE = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_end_of_quote"))
      );
      this.READER_INVALID_ESCAPE = new DynamicCommandExceptionType(
         character -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_escape", new Pair("character", character)))
      );
      this.READER_INVALID_BOOL = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_bool", new Pair("value", value)))
      );
      this.READER_INVALID_INT = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_int", new Pair("value", value)))
      );
      this.READER_EXPECTED_INT = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_int"))
      );
      this.READER_INVALID_LONG = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_long", new Pair("value", value)))
      );
      this.READER_EXPECTED_LONG = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_long"))
      );
      this.READER_INVALID_DOUBLE = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_double", new Pair("value", value)))
      );
      this.READER_EXPECTED_DOUBLE = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_double"))
      );
      this.READER_INVALID_FLOAT = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_invalid_float", new Pair("value", value)))
      );
      this.READER_EXPECTED_FLOAT = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_float"))
      );
      this.READER_EXPECTED_BOOL = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_bool"))
      );
      this.READER_EXPECTED_SYMBOL = new DynamicCommandExceptionType(
         symbol -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_reader_expected_symbol", new Pair("symbol", symbol)))
      );
      this.DISPATCHER_UNKNOWN_COMMAND = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_command"))
      );
      this.DISPATCHER_UNKNOWN_ARGUMENT = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_unknown_argument"))
      );
      this.DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_exception_argument_separator"))
      );
      this.DISPATCHER_PARSE_EXCEPTION = new DynamicCommandExceptionType(
         message -> new LiteralMessage(core.getLanguageHandler().getMessage("command_exception_dispatcher_parse_exception", new Pair("command", message)))
      );
      this.REQUIRE_PLAYER = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_message_require_player")));
      this.PLAYER_NOT_ONLINE = new DynamicCommandExceptionType(
         value -> new LiteralMessage(core.getLanguageHandler().getMessage("command_message_player_not_online", new Pair("name", value)))
      );
      this.CACHE_NOT_FOUND_SELF = new SimpleCommandExceptionType(
         new LiteralMessage(core.getLanguageHandler().getMessage("command_message_cache_not_found_self"))
      );
      this.NO_SELF = new SimpleCommandExceptionType(new LiteralMessage(core.getLanguageHandler().getMessage("command_message_player_no_self")));
      this.CACHE_NOT_FOUND_OTHER = new Dynamic2CommandExceptionType(
         (uuid, name) -> new LiteralMessage(
            core.getLanguageHandler().getMessage("command_message_cache_not_found_other", new Pair("uuid", uuid), new Pair("name", name))
         )
      );
   }

   public Dynamic2CommandExceptionType doubleTooLow() {
      return this.DOUBLE_TOO_SMALL;
   }

   public Dynamic2CommandExceptionType doubleTooHigh() {
      return this.DOUBLE_TOO_BIG;
   }

   public Dynamic2CommandExceptionType floatTooLow() {
      return this.FLOAT_TOO_SMALL;
   }

   public Dynamic2CommandExceptionType floatTooHigh() {
      return this.FLOAT_TOO_BIG;
   }

   public Dynamic2CommandExceptionType integerTooLow() {
      return this.INTEGER_TOO_SMALL;
   }

   public Dynamic2CommandExceptionType integerTooHigh() {
      return this.INTEGER_TOO_BIG;
   }

   public Dynamic2CommandExceptionType longTooLow() {
      return this.LONG_TOO_SMALL;
   }

   public Dynamic2CommandExceptionType longTooHigh() {
      return this.LONG_TOO_BIG;
   }

   public DynamicCommandExceptionType literalIncorrect() {
      return this.LITERAL_INCORRECT;
   }

   public SimpleCommandExceptionType readerExpectedStartOfQuote() {
      return this.READER_EXPECTED_START_OF_QUOTE;
   }

   public SimpleCommandExceptionType readerExpectedEndOfQuote() {
      return this.READER_EXPECTED_END_OF_QUOTE;
   }

   public DynamicCommandExceptionType readerInvalidEscape() {
      return this.READER_INVALID_ESCAPE;
   }

   public DynamicCommandExceptionType readerInvalidBool() {
      return this.READER_INVALID_BOOL;
   }

   public DynamicCommandExceptionType readerInvalidInt() {
      return this.READER_INVALID_INT;
   }

   public SimpleCommandExceptionType readerExpectedInt() {
      return this.READER_EXPECTED_INT;
   }

   public DynamicCommandExceptionType readerInvalidLong() {
      return this.READER_INVALID_LONG;
   }

   public SimpleCommandExceptionType readerExpectedLong() {
      return this.READER_EXPECTED_LONG;
   }

   public DynamicCommandExceptionType readerInvalidDouble() {
      return this.READER_INVALID_DOUBLE;
   }

   public SimpleCommandExceptionType readerExpectedDouble() {
      return this.READER_EXPECTED_DOUBLE;
   }

   public DynamicCommandExceptionType readerInvalidFloat() {
      return this.READER_INVALID_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedFloat() {
      return this.READER_EXPECTED_FLOAT;
   }

   public SimpleCommandExceptionType readerExpectedBool() {
      return this.READER_EXPECTED_BOOL;
   }

   public DynamicCommandExceptionType readerExpectedSymbol() {
      return this.READER_EXPECTED_SYMBOL;
   }

   public SimpleCommandExceptionType dispatcherUnknownCommand() {
      return this.DISPATCHER_UNKNOWN_COMMAND;
   }

   public SimpleCommandExceptionType dispatcherUnknownArgument() {
      return this.DISPATCHER_UNKNOWN_ARGUMENT;
   }

   public SimpleCommandExceptionType dispatcherExpectedArgumentSeparator() {
      return this.DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR;
   }

   public DynamicCommandExceptionType dispatcherParseException() {
      return this.DISPATCHER_PARSE_EXCEPTION;
   }

   public SimpleCommandExceptionType requirePlayer() {
      return this.REQUIRE_PLAYER;
   }

   public DynamicCommandExceptionType playerNotOnline() {
      return this.PLAYER_NOT_ONLINE;
   }

   public SimpleCommandExceptionType noSelf() {
      return this.NO_SELF;
   }

   public SimpleCommandExceptionType cacheNotFoundSelf() {
      return this.CACHE_NOT_FOUND_SELF;
   }

   public Dynamic2CommandExceptionType cacheNotFoundOther() {
      return this.CACHE_NOT_FOUND_OTHER;
   }
}
