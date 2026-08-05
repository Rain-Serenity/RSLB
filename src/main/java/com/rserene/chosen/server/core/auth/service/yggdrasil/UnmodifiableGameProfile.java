package com.rserene.chosen.server.core.auth.service.yggdrasil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.profile.Property;

public class UnmodifiableGameProfile extends GameProfile {
   public UnmodifiableGameProfile(UUID id, String name, Map<String, Property> propertyMap) {
      super(id, name, propertyMap);
   }

   public void setId(UUID id) {
      throw new UnsupportedOperationException();
   }

   public void setName(String name) {
      throw new UnsupportedOperationException();
   }

   public void setPropertyMap(Map<String, Property> propertyMap) {
      throw new UnsupportedOperationException();
   }

   public Map<String, Property> getPropertyMap() {
      Map<String, Property> map = new HashMap<>();

      for (Entry<String, Property> entry : super.getPropertyMap().entrySet()) {
         map.put(entry.getKey(), UnmodifiableGameProfile.UnmodifiableProperty.unmodifiable(entry.getValue()));
      }

      return Collections.unmodifiableMap(map);
   }

   public static UnmodifiableGameProfile unmodifiable(GameProfile profile) {
      return new UnmodifiableGameProfile(profile.getId(), profile.getName(), profile.getPropertyMap());
   }

   public static class UnmodifiableProperty extends Property {
      public UnmodifiableProperty(String name, String value, String signature) {
         super(name, value, signature);
      }

      public static UnmodifiableGameProfile.UnmodifiableProperty unmodifiable(Property property) {
         return new UnmodifiableGameProfile.UnmodifiableProperty(property.getName(), property.getValue(), property.getSignature());
      }

      public void setName(String name) {
         throw new UnsupportedOperationException();
      }

      public void setSignature(String signature) {
         throw new UnsupportedOperationException();
      }

      public void setValue(String value) {
         throw new UnsupportedOperationException();
      }
   }
}
