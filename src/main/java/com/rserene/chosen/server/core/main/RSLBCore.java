package com.rserene.chosen.server.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import lombok.Generated;
import com.rserene.chosen.server.api.RSLBAPI;
import com.rserene.chosen.server.api.RSLBAPIProvider;
import com.rserene.chosen.server.api.data.RSLBPlayerData;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.main.RSLBCoreAPI;
import com.rserene.chosen.server.api.internal.plugin.IPlugin;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.profile.Property;
import com.rserene.chosen.server.core.auth.AuthHandler;
import com.rserene.chosen.server.core.auth.service.yggdrasil.serialize.GameProfileSerializer;
import com.rserene.chosen.server.core.auth.service.yggdrasil.serialize.PropertySerializer;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.configuration.PluginConfig;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.database.SQLManager;
import com.rserene.chosen.server.core.handle.CacheWhitelistHandler;
import com.rserene.chosen.server.core.handle.PlayerHandler;
import com.rserene.chosen.server.core.language.LanguageHandler;
import com.rserene.chosen.server.core.skinrestorer.SkinRestorerCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RSLBCore implements RSLBCoreAPI, RSLBAPI {
   private final IPlugin plugin;
   private final BuildManifest buildManifest;
   private final SQLManager sqlManager;
   private final PluginConfig pluginConfig;
   private final AuthHandler authHandler;
   private final SkinRestorerCore skinRestorerHandler;
   private final CommandHandler commandHandler;
   private final LanguageHandler languageHandler;
   private final PlayerHandler playerHandler;
   private final CacheWhitelistHandler cacheWhitelistHandler;
   private final Gson gson;
   private final String httpRequestHeaderUserAgent = "RSLB/1.1.1";

   public RSLBCore(IPlugin plugin) {
      this.plugin = plugin;
      this.buildManifest = new BuildManifest(this);
      this.languageHandler = new LanguageHandler(this);
      this.pluginConfig = new PluginConfig(plugin.getDataFolder(), this);
      this.sqlManager = new SQLManager(this);
      this.authHandler = new AuthHandler(this);
      this.skinRestorerHandler = new SkinRestorerCore(this);
      this.commandHandler = new CommandHandler(this);
      this.playerHandler = new PlayerHandler(this);
      this.cacheWhitelistHandler = new CacheWhitelistHandler();
      this.gson = new GsonBuilder()
         .setPrettyPrinting()
         .registerTypeAdapter(GameProfile.class, new GameProfileSerializer())
         .registerTypeAdapter(Property.class, new PropertySerializer())
         .create();
   }

   private void showBanner() {
      this.plugin.getRunServer().getConsoleSender().sendMessagePL("\u001b[40;36m[RSLB] 正版与 LittleSkin 登录\u001b[0m");
   }

   public void load() throws IOException, SQLException, ClassNotFoundException, URISyntaxException {
      RSLBAPIProvider.setApi(this);
      this.showBanner();
      this.buildManifest.read();
      this.buildManifest.checkStable();
      this.languageHandler.init();
      this.pluginConfig.reload();
      this.sqlManager.init();
      this.commandHandler.init();
      this.playerHandler.register();
      LoggerProvider.getLogger()
         .info(
            String.format(
               "Loaded, using RSLB v%s on %s - %s",
               this.buildManifest.getVersion(),
               this.plugin.getRunServer().getName(),
               this.plugin.getRunServer().getVersion()
            )
         );
      this.checkEnvironment();
   }

   private void checkEnvironment() {
      if (!this.plugin.getRunServer().isOnlineMode()) {
         LoggerProvider.getLogger().error("Please enable online mode, otherwise the plugin will not work!!!");
         LoggerProvider.getLogger().error("Server is closing!!!");
         throw new EnvironmentException("offline mode.");
      }

      if (!this.plugin.getRunServer().isForwarded()) {
         LoggerProvider.getLogger().error("Please enable forwarding, otherwise the plugin will not work!!!");
         LoggerProvider.getLogger().error("Server is closing!!!");
         throw new EnvironmentException("do not forward.");
      }
   }

   public void reload() throws IOException, URISyntaxException {
      this.pluginConfig.reload();
      this.languageHandler.reload();
   }

   public void close() {
      this.sqlManager.close();
   }

   @NotNull
   public Collection<BaseServiceConfig> getServices() {
      return Collections.unmodifiableCollection(this.pluginConfig.getServiceIdMap().values());
   }

   @Nullable
   public RSLBPlayerData getPlayerData(@NotNull UUID inGameUUID) {
      return this.playerHandler.getPlayerData(inGameUUID);
   }

   @Generated
   public IPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public BuildManifest getBuildManifest() {
      return this.buildManifest;
   }

   @Generated
   public SQLManager getSqlManager() {
      return this.sqlManager;
   }

   @Generated
   public PluginConfig getPluginConfig() {
      return this.pluginConfig;
   }

   @Generated
   public AuthHandler getAuthHandler() {
      return this.authHandler;
   }

   @Generated
   public SkinRestorerCore getSkinRestorerHandler() {
      return this.skinRestorerHandler;
   }

   @Generated
   public CommandHandler getCommandHandler() {
      return this.commandHandler;
   }

   @Generated
   public LanguageHandler getLanguageHandler() {
      return this.languageHandler;
   }

   @Generated
   public PlayerHandler getPlayerHandler() {
      return this.playerHandler;
   }

   @Generated
   public CacheWhitelistHandler getCacheWhitelistHandler() {
      return this.cacheWhitelistHandler;
   }

   @Generated
   public Gson getGson() {
      return this.gson;
   }

   @Generated
   public String getHttpRequestHeaderUserAgent() {
      return "RSLB/1.1.1";
   }
}
