package com.rserene.chosen.server;

import com.rserene.chosen.server.main.RSLBCoreAPI;
import com.rserene.chosen.server.login.LoginHandler;
import com.rserene.chosen.server.command.bukkit.CommandHandler;
import com.rserene.chosen.server.player.event.GlobalListener;
import com.rserene.chosen.server.metrics.Metrics;
import com.rserene.chosen.server.main.RSLBCore;
import java.io.File;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RSLB 主插件入口（Paper 26.2 / Folia）。
 *
 * 启动流程：
 *  1. 初始化 RSLB 核心（RSLBCore）：加载配置、语言文件、认证服务与数据库
 *     （日志直接使用 JavaPlugin.getLogger()，debug 开关由核心 debugEnabled 控制）；
 *  2. 注册事件监听（GlobalListener）与指令（CommandHandler）；
 *  3. 启动登录拦截器（LoginHandler）：包装 netty acceptor，强制所有登录
 *     经过 Yggdrasil 认证后才进入游戏。
 */
public final class RSLB extends JavaPlugin {
    private static final int PLUGIN_ID = 33158;
    private static RSLB instance;
    private RSLBCoreAPI coreAPI;
    private LoginHandler authListener;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.coreAPI = new RSLBCore(this);
            this.coreAPI.load();
            new CommandHandler(this).register();
            new GlobalListener(this).register();
            initAuthListener();
            initMetrics();
        } catch (Throwable e) {
            this.getLogger().severe("An exception was encountered while loading the plugin: " + e.getMessage());
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * 创建并启动登录拦截器。
     * 失败时仅记录错误，不阻止插件本体加载（拦截器缺位会导致所有登录走 vanilla 流程）。
     */
    private void initAuthListener() {
        try {
            this.authListener = new LoginHandler(this);
            this.authListener.start();
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialize login handler: " + e.getMessage());
        }
    }

    /**
     * 初始化 bStats 匿名统计（受配置 settings.metrics-enabled 控制）。
     * 数据上报频率与 opt-out 选项由 bStats 官方 Metrics 类自行管理，本类不做任何改动。
     */
    private void initMetrics() {
        try {
            boolean enabled = this.coreAPI instanceof RSLBCore core
                    && core.getPluginConfig().isMetricsEnabled();
            if (enabled && PLUGIN_ID > 0) {
                new Metrics(this, PLUGIN_ID);
                this.getLogger().info("bStats metrics enabled, data will be submitted anonymously.");
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to initialize bStats metrics: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            if (this.authListener != null) {
                this.authListener.stop();
            }
            if (this.coreAPI != null) {
                this.coreAPI.close();
            }
            Bukkit.getAsyncScheduler().cancelTasks(this);
        } catch (Exception e) {
            this.getLogger().severe("An exception was encountered while closing: " + e.getMessage());
        }
    }

    public boolean isDebugEnabled() {
        return this.coreAPI instanceof RSLBCore core && core.isDebugEnabled();
    }

    public void logDebug(String message) {
        if (this.isDebugEnabled()) {
            this.getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }

    public void logDebug(String message, Throwable throwable) {
        if (this.isDebugEnabled()) {
            this.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
        }
    }

    public void logDebug(Throwable throwable) {
        if (this.isDebugEnabled()) {
            this.getLogger().log(Level.INFO, "[DEBUG] " + throwable, throwable);
        }
    }

    /**
     * 正版模式判定：Bukkit 原生 online-mode，或后端代理已开启转发（BungeeCord / Velocity），
     * 或本插件登录拦截器已生效（此时 MinecraftServer 侧自动开启 online 校验）。
     */
    public boolean isOnlineModeEnvironment() {
        return Bukkit.getOnlineMode() || isBehindProxy() || isAuthListenerActive();
    }

    public boolean isForwardedEnvironment() {
        return true;
    }

    private static boolean isBehindProxy() {
        try {
            File spigotFile = new File("spigot.yml");
            if (spigotFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(spigotFile);
                return config.getBoolean("settings.bungeecord", false);
            }
            File paperFile = new File("paper.yml");
            if (paperFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(paperFile);
                return config.getBoolean(
                    "settings.velocity-support.enabled",
                    config.getBoolean("velocity.enabled", false)
                );
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public String getPluginVersion() {
        return getPluginMeta().getVersion();
    }

    public RSLBCoreAPI getCoreAPI() {
        return this.coreAPI;
    }

    public boolean isAuthListenerActive() {
        return this.authListener != null;
    }

    public static RSLB getInstance() {
        return instance;
    }
}
