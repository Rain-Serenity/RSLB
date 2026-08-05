package com.rserene.chosen.server.bukkit.main;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.handle.HandleResult;
import com.rserene.chosen.server.bukkit.impl.BukkitPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
                event.getPlayer().kickPlayer(result.getKickMessage());
            } else {
                event.getPlayer().kickPlayer("");
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
