package com.rserene.chosen.server.bukkit.impl;

import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.api.internal.plugin.BaseScheduler;
import com.rserene.chosen.server.api.internal.plugin.IPlayerManager;
import com.rserene.chosen.server.api.internal.plugin.ISender;
import com.rserene.chosen.server.api.internal.plugin.IServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class BukkitServer implements IServer {
    private final RSLB plugin;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public BukkitServer(RSLB plugin) {
        this.plugin = plugin;
        this.scheduler = new BukkitScheduler();
        this.playerManager = new BukkitPlayerManager();
    }

    @Override
    public BaseScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getOnlineMode() || this.isBehindProxy() || this.plugin.isAuthListenerActive();
    }

    @Override
    public boolean isForwarded() {
        return true;
    }

    private boolean isBehindProxy() {
        try {
            File spigotFile = new File("spigot.yml");
            if (spigotFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(spigotFile);
                return config.getBoolean("settings.bungeecord", false);
            }
            File paperFile = new File("paper.yml");
            if (paperFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(paperFile);
                return config.getBoolean("settings.velocity-support.enabled",
                    config.getBoolean("velocity.enabled", false));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public String getName() {
        return Bukkit.getName();
    }

    @Override
    public String getVersion() {
        return Bukkit.getVersion();
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public ISender getConsoleSender() {
        return new BukkitSender(Bukkit.getConsoleSender());
    }

    @Override
    public boolean pluginHasEnabled(String id) {
        return Bukkit.getPluginManager().isPluginEnabled(id);
    }
}
