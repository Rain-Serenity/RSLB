package com.rserene.chosen.server;

import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSLBCoreAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.api.internal.plugin.IServer;
import com.rserene.chosen.server.bukkit.auth.LoginHandler;
import com.rserene.chosen.server.bukkit.impl.BukkitServer;
import com.rserene.chosen.server.bukkit.logger.JavaUtilLoggerBridge;
import com.rserene.chosen.server.bukkit.main.CommandHandler;
import com.rserene.chosen.server.bukkit.main.GlobalListener;
import com.rserene.chosen.server.bukkit.metrics.Metrics;
import com.rserene.chosen.server.core.main.RSLBCore;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * RSLB 主插件入口（Bukkit/Folia）。
 *
 * 启动流程：
 *  1. 注册日志桥接（JavaUtilLoggerBridge）与 Bukkit 运行时适配（BukkitServer）；
 *  2. 初始化 RSLB 核心（RSLBCore）：加载配置、语言文件、认证服务与数据库；
 *  3. 注册事件监听（GlobalListener）与指令（CommandHandler）；
 *  4. 启动登录拦截器（LoginHandler）：包装 netty acceptor，强制所有登录
 *     经过 Yggdrasil 认证后才进入游戏。
 */
public final class RSLB extends JavaPlugin implements IPlugin {
    private static final int PLUGIN_ID = 33158;
    private static RSLB instance;
    private BukkitServer runServer;
    private RSLBCoreAPI coreAPI;
    private LoginHandler authListener;

    @Override
    public void onEnable() {
        instance = this;
        try {
            LoggerProvider.setLogger(new JavaUtilLoggerBridge(this.getLogger()));
            this.runServer = new BukkitServer(this);
            this.coreAPI = new RSLBCore(this);
            this.coreAPI.load();
            new GlobalListener(this).register();
            new CommandHandler(this).register("RSLB");
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
        } catch (Exception e) {
            this.getLogger().severe("An exception was encountered while closing: " + e.getMessage());
        }
    }

    @Override
    public IServer getRunServer() {
        return this.runServer;
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
