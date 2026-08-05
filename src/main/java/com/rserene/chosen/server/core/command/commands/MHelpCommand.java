package com.rserene.chosen.server.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.language.LanguageHandler;

/**
 * /rsl help —— 指令帮助列表。
 *
 * 遍历内置指令表，按当前执行者是否拥有对应权限过滤，
 * 只展示有权限使用的指令及其中文说明。
 */
public class MHelpCommand {
   /** [指令名, 所需权限, 说明消息键] */
   private static final String[][] ENTRIES = {
      {"reload", "command.RSLB.reload", "help_reload"},
      {"confirm", "command.RSLB.confirm", "help_confirm"},
      {"list", "command.RSLB.list", "help_list"},
      {"whitelist", "command.RSLB.whitelist.add", "help_whitelist"},
      {"rename", "command.RSLB.rename.oneself", "help_rename"},
      {"info", "command.RSLB.current.oneself", "help_info"},
      {"profile", "command.RSLB.profile.set.oneself", "help_profile"},
      {"find", "command.RSLB.find.profile", "help_find"},
      {"link", "command.RSLB.link.to", "help_link"},
      {"eraseUsername", "command.RSLB.eraseusername", "help_eraseusername"},
      {"eraseAllUsernames", "command.RSLB.eraseallusernames", "help_eraseallusernames"},
      {"help", "command.rslb.base", "help_help"},
   };

   private final CommandHandler handler;

   public MHelpCommand(CommandHandler handler) {
      this.handler = handler;
   }

   public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
      return (LiteralArgumentBuilder<ISender>)literalArgumentBuilder.then(this.handler.literal("help").executes(this::executeHelp));
   }

   private int executeHelp(CommandContext<ISender> context) {
      ISender sender = (ISender)context.getSource();
      LanguageHandler lang = CommandHandler.getCore().getLanguageHandler();
      List<String> lines = new ArrayList<>();
      for (String[] entry : ENTRIES) {
         if (sender.hasPermission(entry[1])) {
            lines.add(lang.getMessage("help_entry", new Pair("command", entry[0]), new Pair("description", lang.getMessage(entry[2]))));
         }
      }

      sender.sendMessagePL(lang.getMessage("help_header", new Pair("count", lines.size())));
      for (String line : lines) {
         sender.sendMessagePL(line);
      }
      sender.sendMessagePL(lang.getMessage("help_footer"));
      return 0;
   }
}
