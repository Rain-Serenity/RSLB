package com.rserene.chosen.server.core.configuration;

import java.io.File;
import java.util.TreeMap;
import java.util.Map.Entry;
import lombok.Generated;
import com.rserene.chosen.server.api.MapperConfigAPI;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader.Builder;

public class MapperConfig implements MapperConfigAPI {
   private final TreeMap<Integer, Integer> packetMapping = new TreeMap<Integer, Integer>() {
      {
         this.put(761, 32);
         this.put(762, 6);
         this.put(765, 7);
         this.put(768, 8);
         this.put(771, 9);
      }

      public Integer put(Integer key, Integer value) {
         if (key < 761) {
            return value;
         }

         if (this.containsValue(value)) {
            Integer existingKey = this.findKeyByValue(value);
            if (existingKey != null && existingKey > key) {
               super.remove(existingKey);
               super.put(key, value);
            }

            return value;
         } else {
            return super.put(key, value);
         }
      }

      private Integer findKeyByValue(Integer value) {
         for (Entry<Integer, Integer> entry : this.entrySet()) {
            if (entry.getValue().equals(value)) {
               return entry.getKey();
            }
         }

         return null;
      }
   };
   private final File dataFolder;

   MapperConfig(File dataFolder) {
      this.dataFolder = dataFolder;
   }

   public void save() {
      try {
         YamlConfigurationLoader loader = ((Builder)YamlConfigurationLoader.builder().file(new File(this.dataFolder, "mapper.yml"))).indent(2).build();
         CommentedConfigurationNode rootNode = (CommentedConfigurationNode)loader.load();
         CommentedConfigurationNode mapperNode = (CommentedConfigurationNode)rootNode.node(new Object[]{"mapper"});

         for (Entry<Integer, Integer> entry : this.packetMapping.entrySet()) {
            ((CommentedConfigurationNode)mapperNode.node(new Object[]{entry.getKey().toString()})).set(String.format("0x%02X", entry.getValue()));
         }

         loader.save(rootNode);
      } catch (ConfigurateException e) {
         throw new RuntimeException(e);
      }
   }

   public void reload() {
      YamlConfigurationLoader loader = ((Builder)YamlConfigurationLoader.builder().file(new File(this.dataFolder, "mapper.yml"))).build();

      try {
         ConfigurationNode mapperNode = ((CommentedConfigurationNode)loader.load()).node(new Object[]{"mapper"});

         for (Entry<Object, ? extends ConfigurationNode> entry : mapperNode.childrenMap().entrySet()) {
            String key = entry.getKey().toString();
            String hexValue = entry.getValue().getString();
            if (hexValue != null) {
               int intValue = Integer.decode(hexValue);
               this.packetMapping.put(Integer.parseInt(key), intValue);
            }
         }
      } catch (ConfigurateException e) {
         throw new RuntimeException(e);
      }
   }

   @Generated
   public TreeMap<Integer, Integer> getPacketMapping() {
      return this.packetMapping;
   }

   @Generated
   public File getDataFolder() {
      return this.dataFolder;
   }

   @Generated
   @Override
   public String toString() {
      return "MapperConfig(packetMapping=" + this.getPacketMapping() + ", dataFolder=" + this.getDataFolder() + ")";
   }
}
