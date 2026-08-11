package com.rserene.chosen.server.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Generated;
import com.rserene.chosen.server.util.IOUtil;
import com.rserene.chosen.server.service.ServiceType;
import com.rserene.chosen.server.config.service.BaseServiceConfig;
import com.rserene.chosen.server.config.service.yggdrasil.LittleSkinYggdrasilServiceConfig;
import com.rserene.chosen.server.config.service.yggdrasil.OfficialYggdrasilServiceConfig;
import com.rserene.chosen.server.main.RSLBCore;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader.Builder;

public class PluginConfig {
   private final File dataFolder;
   private static final Map<ServiceType, String> onlyOneServiceInfoMap = Map.of(ServiceType.OFFICIAL, "official");
   private boolean nameCorrect;
   private boolean autoNameChange;
   private SqlConfig sqlConfig;
   private String nameAllowedRegular;
   private final RSLBCore core;
   private boolean welcomeMsg;
   private Map<Integer, BaseServiceConfig> serviceIdMap = new HashMap<>();
   private long confirmCommandValidTimeMills;
   private long linkAcceptValidTimeMills;
   private boolean metricsEnabled;
   private boolean profileKeyVerify;

   public PluginConfig(File dataFolder, RSLBCore core) {
      this.dataFolder = dataFolder;
      this.core = core;
   }

   public void reload() throws IOException, URISyntaxException {
      File servicesFolder = new File(this.dataFolder, "services");
      if (!this.dataFolder.exists()) {
         Files.createDirectory(this.dataFolder.toPath());
      }

      if (!servicesFolder.exists()) {
         Files.createDirectory(servicesFolder.toPath());
      }

      IOUtil.removeAllFiles(new File(this.dataFolder, "examples"));
      this.saveResource("config.yml", false);
      this.saveResource("services/official.yml", false);
      this.saveResource("services/littleskin.yml", false);
      CommentedConfigurationNode configConfigurationNode = (CommentedConfigurationNode)((Builder)YamlConfigurationLoader.builder()
            .file(new File(this.dataFolder, "config.yml")))
         .build()
         .load();
      if (((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "debug"})).getBoolean(false)) {
         this.core.setDebugEnabled(true);
      } else {
         this.core.setDebugEnabled(false);
      }

      this.sqlConfig = SqlConfig.read((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"database"}));
      this.nameAllowedRegular = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "name-allowed-regular"}))
         .getString("^[0-9a-zA-Z_]{3,16}$");
      this.welcomeMsg = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "welcome-message"})).getBoolean(true);
      this.nameCorrect = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "auto-correct-name"})).getBoolean(true);
      this.autoNameChange = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "auto-name-change"})).getBoolean(true);
      this.confirmCommandValidTimeMills = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "confirm-command-valid-mills"}))
         .getLong(15000L);
      this.linkAcceptValidTimeMills = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "link-accept-valid-mills"})).getLong(30000L);
      this.metricsEnabled = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "metrics-enabled"})).getBoolean(true);
      this.profileKeyVerify = ((CommentedConfigurationNode)configConfigurationNode.node(new Object[]{"settings", "profile-key-verify"})).getBoolean(false);
      Map<Integer, BaseServiceConfig> idMap = new HashMap<>();

      try (Stream<Path> list = Files.list(servicesFolder.toPath())) {
         List<BaseServiceConfig> tmp = new ArrayList<>();
         Set<String> builtInServiceFiles = Set.of("official.yml", "littleskin.yml");
         list.forEach(path -> {
            if (builtInServiceFiles.contains(path.toFile().getName().toLowerCase(Locale.ROOT))) {
               try {
                  tmp.add(this.readServiceConfig((CommentedConfigurationNode)((Builder)YamlConfigurationLoader.builder().path(path)).build().load()));
               } catch (Exception e) {
                  this.core.getLogger().log(java.util.logging.Level.SEVERE, "Unable to read authentication service config under file " + path, e);
               }
            }
         });
         Set<ServiceType> notRepeat = new HashSet<>();

         for (BaseServiceConfig config : tmp) {
            if (onlyOneServiceInfoMap.containsKey(config.getServiceType()) && !notRepeat.add(config.getServiceType())) {
               throw new ConfException(
                  String.format(
                     "Duplicates are not allowed for authentication services of type %s, but more than one was found.",
                     onlyOneServiceInfoMap.get(config.getServiceType())
                  )
               );
            }
         }

         for (BaseServiceConfig config : tmp) {
            if (idMap.containsKey(config.getId())) {
               throw new ConfException(String.format("The same authentication service id value %d exists.", config.getId()));
            }

            idMap.put(config.getId(), config);
         }
      }

      idMap.forEach((i, y) -> {
         if (y.getName().equalsIgnoreCase("unnamed")) {
            this.core.getLogger().warning(String.format("The name of authentication service whose id is %d has not been set.", i));
         }

         this.core.getLogger().info(String.format("Add a authentication service with id %d and name %s.", i, y.getName()));
      });
      if (idMap.size() == 0) {
         this.core.getLogger().warning("The server has not added any authentication service, which will prevent all players from logging in.");
      } else {
         this.core.getLogger().info(String.format("Added %d authentication services.", idMap.size()));
      }

      this.serviceIdMap = Collections.unmodifiableMap(idMap);
   }

   private BaseServiceConfig readServiceConfig(CommentedConfigurationNode load) throws SerializationException, ConfException {
      CommentedConfigurationNode nodeId = (CommentedConfigurationNode)load.node(new Object[]{"id"});
      if (nodeId.empty()) {
         throw new ConfException("service id is null.");
      }

      int id = nodeId.getInt();
      String name = ((CommentedConfigurationNode)load.node(new Object[]{"name"})).getString("Unnamed");
      ServiceType serviceType = (ServiceType)((CommentedConfigurationNode)load.node(new Object[]{"serviceType"})).get(ServiceType.class);
      if (serviceType == null) {
         throw new ConfException("service type is null.");
      }

      BaseServiceConfig.InitUUID initUUID = (BaseServiceConfig.InitUUID)((CommentedConfigurationNode)load.node(new Object[]{"initUUID"}))
         .get(BaseServiceConfig.InitUUID.class, BaseServiceConfig.InitUUID.DEFAULT);
      boolean whitelist = ((CommentedConfigurationNode)load.node(new Object[]{"whitelist"})).getBoolean(false);
      SkinRestorerConfig skinRestorer = SkinRestorerConfig.read((CommentedConfigurationNode)load.node(new Object[]{"skinRestorer"}));
      String initNameFormat = ((CommentedConfigurationNode)load.node(new Object[]{"initNameFormat"})).getString("{name}");
      if (serviceType.isYggdrasilService()) {
         CommentedConfigurationNode yggdrasilAuthNode = (CommentedConfigurationNode)load.node(new Object[]{"yggdrasilAuth"});
         boolean trackIp = ((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"trackIp"})).getBoolean(false);
         int timeout = ((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"timeout"})).getInt(10000);
         int retry = ((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"retry"})).getInt(0);
         long retryDelay = ((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"retryDelay"})).getLong(0L);
         ProxyConfig authProxy = ProxyConfig.read((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"authProxy"}));
         if (serviceType == ServiceType.OFFICIAL) {
            String customSessionServer = ((CommentedConfigurationNode)((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"official"}))
                  .node(new Object[]{"sessionServer"}))
               .getString("https://sessionserver.mojang.com");
            return new OfficialYggdrasilServiceConfig(
               id, name, initUUID, initNameFormat, whitelist, skinRestorer, trackIp, timeout, retry, retryDelay, authProxy, customSessionServer
            );
         }

         if (serviceType == ServiceType.LITTLESKIN) {
            return new LittleSkinYggdrasilServiceConfig(
               id,
               name,
               initUUID,
               initNameFormat,
               whitelist,
               skinRestorer,
               trackIp,
               timeout,
               retry,
               retryDelay,
               authProxy,
               ((CommentedConfigurationNode)((CommentedConfigurationNode)yggdrasilAuthNode.node(new Object[]{"littleSkin"})).node(new Object[]{"apiRoot"}))
                  .getString()
            );
         }
      }

      throw new ConfException("Unknown service type " + serviceType.name());
   }

   public void saveResource(String path, boolean cover) throws IOException {
      this.saveResource(cover, this.dataFolder, path, path);
   }

   private void saveResource(boolean cover, File file, String realName, String fileName) throws IOException {
      File subFile = new File(file, fileName);
      boolean exists = subFile.exists();
      if (!exists || cover) {
         if (!exists) {
            Files.createFile(subFile.toPath());
         }

         try (
            InputStream is = Objects.requireNonNull(this.getClass().getResourceAsStream("/" + realName));
            FileOutputStream fs = new FileOutputStream(subFile);
         ) {
            IOUtil.copy(is, fs);
         }

         if (!exists) {
            this.core.getLogger().info("Extract: " + realName);
         } else {
            this.core.getLogger().info("Cover: " + realName);
         }
      }
   }

   @Generated
   public boolean isNameCorrect() {
      return this.nameCorrect;
   }

   @Generated
   public boolean isAutoNameChange() {
      return this.autoNameChange;
   }

   @Generated
   public SqlConfig getSqlConfig() {
      return this.sqlConfig;
   }

   @Generated
   public String getNameAllowedRegular() {
      return this.nameAllowedRegular;
   }

   @Generated
   public boolean isWelcomeMsg() {
      return this.welcomeMsg;
   }

   @Generated
   public Map<Integer, BaseServiceConfig> getServiceIdMap() {
      return this.serviceIdMap;
   }

   @Generated
   public long getConfirmCommandValidTimeMills() {
      return this.confirmCommandValidTimeMills;
   }

   @Generated
   public long getLinkAcceptValidTimeMills() {
      return this.linkAcceptValidTimeMills;
   }

   @Generated
   public boolean isMetricsEnabled() {
      return this.metricsEnabled;
   }

   @Generated
   public boolean isProfileKeyVerify() {
      return this.profileKeyVerify;
   }
}
