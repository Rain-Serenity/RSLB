package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitSender implements ISender {
    private final CommandSender sender;

    public BukkitSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean isPlayer() {
        return this.sender instanceof Player;
    }

    @Override
    public boolean isConsole() {
        return !this.isPlayer();
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public void sendMessagePL(String message) {
        for (String s : message.split("\\r?\\n")) {
            this.sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
        }
    }

    @Override
    public String getName() {
        return this.sender.getName();
    }

    @Override
    public IPlayer getAsPlayer() {
        return new BukkitPlayer((Player) this.sender);
    }
}
