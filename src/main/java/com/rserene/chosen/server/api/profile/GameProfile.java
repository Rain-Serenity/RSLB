package com.rserene.chosen.server.api.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;

public class GameProfile {
   private UUID id;
   private String name;
   private Map<String, Property> propertyMap;

   public GameProfile clone() {
      GameProfile response = new GameProfile(this.id, this.name, new HashMap<>());

      for (Entry<String, Property> entry : this.propertyMap.entrySet()) {
         response.propertyMap.put(entry.getKey(), entry.getValue().clone());
      }

      return response;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         GameProfile that = (GameProfile)o;
         return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.propertyMap, that.propertyMap);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.name, this.propertyMap);
   }

   @Generated
   public UUID getId() {
      return this.id;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Map<String, Property> getPropertyMap() {
      return this.propertyMap;
   }

   @Generated
   public void setId(UUID id) {
      this.id = id;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setPropertyMap(Map<String, Property> propertyMap) {
      this.propertyMap = propertyMap;
   }

   @Generated
   @Override
   public String toString() {
      return "GameProfile(id=" + this.getId() + ", name=" + this.getName() + ", propertyMap=" + this.getPropertyMap() + ")";
   }

   @Generated
   public GameProfile(UUID id, String name, Map<String, Property> propertyMap) {
      this.id = id;
      this.name = name;
      this.propertyMap = propertyMap;
   }

   @Generated
   public GameProfile() {
   }
}
