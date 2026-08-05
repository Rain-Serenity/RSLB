package com.rserene.chosen.server;

import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSLVCoreAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.api.internal.plugin.IServer;
import com.rserene.chosen.server.bukkit.auth.LoginHandler;
import com.rserene.chosen.server.bukkit.impl.BukkitServer;
import com.rserene.chosen.server.bukkit.logger.JavaUtilLoggerBridge;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class RSLB extends JavaPlugin implements IPlugin {
    private static RSLB instance;
    private BukkitServer runServer;
    private RSLVCoreAPI RSLVCoreAPI;
    private LoginHandler authListener;

    @Override
    public void onEnable() {
        instance = this;
        try {
            LoggerProvider.setLogger(new JavaUtilLoggerBridge(this.getLogger()));
            this.runServer = new BukkitServer(this);
            this.RSLVCoreAPI = new com.rserene.chosen.server.core.main.RSLVCore(this);
            this.RSLVCoreAPI.load();
            new com.rserene.chosen.server.bukkit.main.GlobalListener(this).register();
            new com.rserene.chosen.server.bukkit.main.CommandHandler(this).register("RSLB");
            initAuthListener();
        } catch (Throwable e) {
            this.getLogger().severe("An exception was encountered while loading the plugin: " + e.getMessage());
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void initAuthListener() {
        try {
            this.authListener = new LoginHandler(this);
            this.authListener.start();
            this.getLogger().info("Login handler enabled - intercepting login for multi-Yggdrasil auth");
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialize login handler: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            if (this.authListener != null) {
                this.authListener.stop();
            }
            if (this.RSLVCoreAPI != null) {
                this.RSLVCoreAPI.close();
            }
        } catch (Exception e) {
            this.getLogger().severe("An exception was encountered while closing: " + e.getMessage());
        }
    }

    @Override
    public File getTempFolder() {
        return new File(this.getDataFolder(), "tmp");
    }

    @Override
    public IServer getRunServer() {
        return this.runServer;
    }

    public RSLVCoreAPI getRSLVCoreAPI() {
        return this.RSLVCoreAPI;
    }

    public boolean isAuthListenerActive() {
        return this.authListener != null;
    }

    public static RSLB getInstance() {
        return instance;
    }
}
