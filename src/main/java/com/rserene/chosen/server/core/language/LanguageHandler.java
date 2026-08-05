package com.rserene.chosen.server.core.language;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.rserene.chosen.server.api.internal.language.LanguageAPI;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.IOUtil;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.main.RSLVCore;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader.Builder;

public class LanguageHandler implements LanguageAPI {
   private final RSLVCore core;
   private Map<String, String> language;

   public LanguageHandler(RSLVCore core) {
      this.core = core;
   }

   public void init() throws IOException {
      this.reload();
   }

   public final String getMessage(String node, Pair<?, ?>... pairs) {
      return ValueUtil.transPapi(this.language.get(node), pairs);
   }

   public void reload() throws IOException {
      Map<String, String> tmp = new HashMap<>();
      File messagesFile = new File(this.core.getPlugin().getDataFolder(), "messages.yml");
      if (!messagesFile.exists()) {
         File parentDir = messagesFile.getParentFile();
         if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
         }
         try (
            OutputStream outputStream = new FileOutputStream(messagesFile);
            InputStream resourceAsStream = Objects.requireNonNull(this.getClass().getResourceAsStream("/messages.yml"));
         ) {
            IOUtil.copy(resourceAsStream, outputStream);
         }

         LoggerProvider.getLogger().info("Extract: messages.yml");
      }

      File legacyFile = new File(this.core.getPlugin().getDataFolder(), "message.properties");
      if (legacyFile.exists()) {
         legacyFile.delete();
         LoggerProvider.getLogger().info("Removed legacy message.properties, language file is now messages.yml");
      }

      CommentedConfigurationNode loaded = (CommentedConfigurationNode)((Builder)YamlConfigurationLoader.builder().file(messagesFile)).build().load();
      tmp.putAll(flatten(loaded, ""));

      try (
         InputStream var19 = Objects.requireNonNull(this.getClass().getResourceAsStream("/messages.yml"));
      ) {
         CommentedConfigurationNode inside = (CommentedConfigurationNode)((Builder)YamlConfigurationLoader.builder()
            .source(() -> new java.io.BufferedReader(new java.io.InputStreamReader(var19, java.nio.charset.StandardCharsets.UTF_8))))
            .build()
            .load();
         Map<String, String> defaults = flatten(inside, "");

         for (Map.Entry<String, String> entry : defaults.entrySet()) {
            if (!tmp.containsKey(entry.getKey())) {
               tmp.put(entry.getKey(), entry.getValue());
               LoggerProvider.getLogger().warn("Missing message from node " + entry.getKey());
            }
         }
      }

      this.language = tmp;
   }

   private static Map<String, String> flatten(CommentedConfigurationNode node, String prefix) {
      Map<String, String> result = new HashMap<>();
      for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
         String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey().toString();
         CommentedConfigurationNode child = entry.getValue();
         if (child.isMap()) {
            result.putAll(flatten(child, key));
         } else {
            String value = child.getString();
            if (value != null) {
               result.put(key, value);
            }
         }
      }
      return result;
   }
}
