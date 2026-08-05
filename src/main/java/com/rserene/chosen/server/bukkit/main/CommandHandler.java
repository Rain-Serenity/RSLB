package com.rserene.chosen.server.bukkit.main;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.bukkit.impl.BukkitSender;
import java.util.List;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final RSLB plugin;
    private com.rserene.chosen.server.api.internal.command.CommandAPI commandHandler;

    public CommandHandler(RSLB plugin) {
        this.plugin = plugin;
    }

    public void register(String name) {
        this.commandHandler = this.plugin.getRSLVCoreAPI().getCommandHandler();
        this.plugin.getCommand(name.toLowerCase()).setExecutor(this);
        this.plugin.getCommand(name.toLowerCase()).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            String cmd = label.equalsIgnoreCase("rslb") ? "rsl" : label;
            String[] fullArgs = new String[args.length + 1];
            fullArgs[0] = cmd;
            System.arraycopy(args, 0, fullArgs, 1, args.length);
            this.commandHandler.execute(new BukkitSender(sender), fullArgs);
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception occurred while executing command.", e);
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&b&lRSLB&8]&r&c处理指令时发生异常，请与服务器管理员取得联系。"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        try {
            String cmd = alias.equalsIgnoreCase("rslb") ? "rsl" : alias;
            String[] fullArgs = new String[args.length + 1];
            fullArgs[0] = cmd;
            System.arraycopy(args, 0, fullArgs, 1, args.length);
            return this.commandHandler.tabComplete(new BukkitSender(sender), fullArgs);
        } catch (Exception e) {
            return List.of();
        }
    }
}
