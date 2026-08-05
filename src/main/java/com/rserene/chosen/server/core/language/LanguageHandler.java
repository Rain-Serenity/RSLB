package com.rserene.chosen.server.core.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.Map.Entry;
import com.rserene.chosen.server.api.internal.language.LanguageAPI;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.IOUtil;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.main.RSLVCore;

public class LanguageHandler implements LanguageAPI {
   private final RSLVCore core;
   private Properties language;

   public LanguageHandler(RSLVCore core) {
      this.core = core;
   }

   public void init() throws IOException {
      this.reload();
   }

   public final String getMessage(String node, Pair<?, ?>... pairs) {
      return ValueUtil.transPapi(this.language.getProperty(node), pairs);
   }

   public void reload() throws IOException {
      Properties tmp = new Properties();
      File messagePropertiesFile = new File(this.core.getPlugin().getDataFolder(), "message.properties");
      if (!messagePropertiesFile.exists()) {
         File parentDir = messagePropertiesFile.getParentFile();
         if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
         }
         try (
            OutputStream outputStream = new FileOutputStream(messagePropertiesFile);
            InputStream resourceAsStream = Objects.requireNonNull(this.getClass().getResourceAsStream("/message.properties"));
         ) {
            IOUtil.copy(resourceAsStream, outputStream);
         }

         LoggerProvider.getLogger().info("Extract: message.properties");
      }

      try (InputStream inputStream = new FileInputStream(messagePropertiesFile)) {
         tmp.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
      }

      try (
         InputStream var19 = Objects.requireNonNull(this.getClass().getResourceAsStream("/message.properties"));
         InputStreamReader isr = new InputStreamReader(var19, StandardCharsets.UTF_8);
      ) {
         Properties inside = new Properties();
         inside.load(isr);

         for (Entry<Object, Object> entry : inside.entrySet()) {
            if (!tmp.containsKey(entry.getKey())) {
               tmp.setProperty(entry.getKey().toString(), entry.getValue().toString());
               LoggerProvider.getLogger().warn("Missing message from node " + entry.getKey().toString());
            }
         }
      }

      this.language = tmp;
   }
}
