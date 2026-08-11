package com.rserene.chosen.server.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CommandAPI {
   void execute(CommandSender var1, String[] var2);

   void execute(CommandSender var1, String var2);

   List<String> tabComplete(CommandSender var1, String[] var2);

   List<String> tabComplete(CommandSender var1, String var2);
}
