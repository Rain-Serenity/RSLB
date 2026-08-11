package com.rserene.chosen.server.player.event;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.player.HandleResult;
import com.rserene.chosen.server.player.HandlerAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 全局 Bukkit 事件监听器：负责玩家进出游戏时的核心流程推送。
 */
public final class GlobalListener implements Listener {
    private final RSLB plugin;

    public GlobalListener(RSLB plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        HandleResult result = this.plugin.getCoreAPI().getPlayerHandler()
                .pushPlayerJoinGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (result.getType() != HandleResult.Type.KICK) {
            this.plugin.getCoreAPI().getPlayerHandler().callPlayerJoinGame(event.getPlayer());
        } else if (result.getKickMessage() != null && !result.getKickMessage().trim().isEmpty()) {
            event.getPlayer().kick(LegacyComponentSerializer.legacyAmpersand().deserialize(result.getKickMessage()));
        } else {
            event.getPlayer().kick(Component.empty());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.getCoreAPI().getPlayerHandler()
                .pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
