package com.rserene.chosen.server.core.main;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;

public class BuildManifest {
   private final RSLBCore core;
   private String buildType;
   private Date buildDate;
   private String version;

   public BuildManifest(RSLBCore core) {
      this.core = core;
   }

   public void read() throws IOException {
      Properties properties = new Properties();
      properties.load(this.getClass().getResourceAsStream("/build.properties"));
      this.buildType = properties.getProperty("build_type");
      this.buildDate = new Date(Long.parseLong(properties.getProperty("build_timestamp")));
      this.version = properties.getProperty("version");
   }

   public void checkStable() {
      if (!this.buildType.equalsIgnoreCase("final")) {
         LoggerProvider.getLogger().warn("当前 RSLB 构建不是正式版本，请谨慎用于生产环境。");
      }
   }

   @Generated
   public RSLBCore getCore() {
      return this.core;
   }

   @Generated
   public String getBuildType() {
      return this.buildType;
   }

   @Generated
   public Date getBuildDate() {
      return this.buildDate;
   }

   @Generated
   public String getVersion() {
      return this.version;
   }
}
