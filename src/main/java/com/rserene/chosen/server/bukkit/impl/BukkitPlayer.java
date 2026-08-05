package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class BukkitPlayer implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        this.player.kickPlayer(message);
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public SocketAddress getAddress() {
        return this.player.getAddress();
    }

    @Override
    public boolean isOnline() {
        return RSLB.getInstance().getRunServer().getPlayerManager().getPlayer(this.player.getUniqueId()) != null;
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }

    @Override
    public void sendMessagePL(String message) {
        for (String s : message.split("\\r?\\n")) {
            this.player.sendMessage(Component.text(s));
        }
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public IPlayer getAsPlayer() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BukkitPlayer that = (BukkitPlayer) o;
        return Objects.equals(this.player.getUniqueId(), that.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.player.getUniqueId());
    }
}
