package com.rserene.chosen.server.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import com.rserene.chosen.server.util.Pair;
import com.rserene.chosen.server.command.CommandHandler;
import com.rserene.chosen.server.language.LanguageHandler;
import com.rserene.chosen.server.util.MessageUtil;
import org.bukkit.command.CommandSender;

/**
 * /rslb help —— 指令帮助列表。
 *
 * 遍历内置指令表，按当前执行者是否拥有对应权限过滤，
 * 只展示有权限使用的指令及其中文说明。
 */
public class MHelpCommand {
   /** [指令名, 所需权限, 说明消息键] */
   private static final String[][] ENTRIES = {
      {"reload", "rslb.reload", "help_reload"},
      {"confirm", "rslb.confirm", "help_confirm"},
      {"list", "rslb.list", "help_list"},
      {"whitelist", "rslb.whitelist.add", "help_whitelist"},
      {"rename", "rslb.rename.oneself", "help_rename"},
      {"info", "rslb.info.oneself", "help_info"},
      {"profile", "rslb.profile.set.oneself", "help_profile"},
      {"find", "rslb.find.profile", "help_find"},
      {"link", "rslb.link.to", "help_link"},
      {"eraseUsername", "rslb.erase.username", "help_eraseusername"},
      {"eraseAllUsernames", "rslb.erase.all", "help_eraseallusernames"},
      {"help", "rslb.base", "help_help"},
   };

   private final CommandHandler handler;

   public MHelpCommand(CommandHandler handler) {
      this.handler = handler;
   }

    public LiteralArgumentBuilder<CommandSender> register(LiteralArgumentBuilder<CommandSender> literalArgumentBuilder) {
       return (LiteralArgumentBuilder<CommandSender>)literalArgumentBuilder.executes(this::executeHelp);
    }

   private int executeHelp(CommandContext<CommandSender> context) {
      CommandSender sender = context.getSource();
      LanguageHandler lang = CommandHandler.getCore().getLanguageHandler();
      List<String> lines = new ArrayList<>();
      for (String[] entry : ENTRIES) {
         if (sender.hasPermission(entry[1])) {
            lines.add(lang.getMessage("help_entry", new Pair("command", entry[0]), new Pair("description", lang.getMessage(entry[2]))));
         }
      }

      MessageUtil.sendLegacy(sender, lang.getMessage("help_header", new Pair("count", lines.size())));
      for (String line : lines) {
         MessageUtil.sendLegacy(sender, line);
      }
      MessageUtil.sendLegacy(sender, lang.getMessage("help_footer"));
      return 0;
   }
}
