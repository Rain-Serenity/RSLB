package com.rserene.chosen.server.bukkit.main;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.handle.HandleResult;
import com.rserene.chosen.server.bukkit.impl.BukkitPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家进出服事件桥接。
 *
 * 将 Bukkit 的 PlayerJoin/PlayerQuit 事件转发给 RSLV 核心的 PlayerHandler，
 * 完成入服档案注册（推送登录数据）与离服数据清理。
 * 加入时若核心判定需要踢出（如白名单被删、档案被回收），在此统一执行。
 */
public class GlobalListener implements Listener {
    private final RSLB plugin;

    public GlobalListener(RSLB plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        HandleResult result = this.plugin
            .getRSLVCoreAPI()
            .getPlayerHandler()
            .pushPlayerJoinGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (result.getType() != HandleResult.Type.KICK) {
            this.plugin.getRSLVCoreAPI().getPlayerHandler().callPlayerJoinGame(
                new BukkitPlayer(event.getPlayer())
            );
        } else {
            if (result.getKickMessage() != null && !result.getKickMessage().trim().isEmpty()) {
                event.getPlayer().kick(LegacyComponentSerializer.legacyAmpersand().deserialize(result.getKickMessage()));
            } else {
                event.getPlayer().kick(Component.empty());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin
            .getRSLVCoreAPI()
            .getPlayerHandler()
            .pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
