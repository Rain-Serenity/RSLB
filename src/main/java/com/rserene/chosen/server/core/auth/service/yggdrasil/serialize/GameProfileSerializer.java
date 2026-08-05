package com.rserene.chosen.server.core.auth.service.yggdrasil.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.profile.Property;
import com.rserene.chosen.server.core.auth.service.yggdrasil.UnmodifiableGameProfile;

public class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
   public GameProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      GameProfile ret = new GameProfile();
      HashMap<String, Property> propertyMap = new HashMap<>();
      ret.setPropertyMap(propertyMap);
      if (json.isJsonObject()) {
         JsonObject root = json.getAsJsonObject();
         ret.setId(ValueUtil.getUuidOrNull(root.get("id").getAsString()));
         if (root.has("name")) {
            ret.setName(root.get("name").getAsString());
         }

         JsonElement propertiesJsonElement = root.get("properties");
         if (propertiesJsonElement != null) {
            if (propertiesJsonElement.isJsonObject()) {
               JsonObject object = propertiesJsonElement.getAsJsonObject();

               for (Entry<String, JsonElement> entry : object.entrySet()) {
                  if (entry.getValue().isJsonArray()) {
                     for (JsonElement ignored : entry.getValue().getAsJsonArray()) {
                        propertyMap.put(entry.getKey(), (Property)context.deserialize(ignored, Property.class));
                     }
                  }
               }
            } else if (propertiesJsonElement.isJsonArray()) {
               for (JsonElement element : propertiesJsonElement.getAsJsonArray()) {
                  Property value = (Property)context.deserialize(element, Property.class);
                  propertyMap.put(value.getName(), value);
               }
            }
         }
      }

      return UnmodifiableGameProfile.unmodifiable(ret);
   }

   public JsonElement serialize(GameProfile src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject ret = new JsonObject();
      ret.addProperty("id", src.getId().toString().replace("-", ""));
      ret.addProperty("name", src.getName());
      JsonArray propertiesJsonArray = new JsonArray();
      ret.add("properties", propertiesJsonArray);

      for (Entry<String, Property> entry : src.getPropertyMap().entrySet()) {
         propertiesJsonArray.add(context.serialize(entry.getValue(), Property.class));
      }

      return ret;
   }
}
