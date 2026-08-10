package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.plugin.IPlayer;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * IPlayer 的 Bukkit 实现：包装在线玩家实体。
 *
 * 提供核心所需的玩家信息查询（UUID、地址、在线状态）与消息发送；
 * 消息同样经 legacy '&' 颜色码解析后发送。
 */
public class BukkitPlayer implements IPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void kickPlayer(String message) {
        // 核心命令在异步线程执行，而玩家踢出只能在实体所在线程（Paper 主线程 / Folia 区域线程）操作。
        // 统一经 RegionScheduler 调度（Paper 26.2 与 Folia 均支持），任务内部再次校验在线状态。
        Player p = this.player;
        Location location = p.getLocation();
        Bukkit.getRegionScheduler().run(RSLB.getInstance(), location, task -> {
            if (p.isOnline()) {
                Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                p.kick(component);
            }
        });
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
            this.player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
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
