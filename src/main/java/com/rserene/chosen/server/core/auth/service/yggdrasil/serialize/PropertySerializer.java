package com.rserene.chosen.server.core.auth.service.yggdrasil.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import lombok.Generated;
import com.rserene.chosen.server.api.profile.Property;
import com.rserene.chosen.server.core.auth.service.yggdrasil.UnmodifiableGameProfile;

public class PropertySerializer implements JsonSerializer<Property>, JsonDeserializer<Property> {
   public Property deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      Property ret = new Property();
      if (json.isJsonObject()) {
         JsonObject root = json.getAsJsonObject();
         ret.setName(root.get("name").getAsString());
         ret.setValue(root.get("value").getAsString());
         if (root.has("signature")) {
            ret.setSignature(root.get("signature").getAsString());
         }
      }

      return UnmodifiableGameProfile.UnmodifiableProperty.unmodifiable(ret);
   }

   public JsonElement serialize(Property src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject ret = new JsonObject();
      ret.addProperty("name", src.getName());
      ret.addProperty("value", src.getValue());
      if (src.getSignature() != null) {
         ret.addProperty("signature", src.getSignature());
      }

      return ret;
   }
}
