package com.rserene.chosen.server.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.api.profile.Property;
import com.rserene.chosen.server.core.configuration.SkinRestorerConfig;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.main.RSLVCore;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class SkinRestorerFlows implements Callable<SkinRestorerResultImpl> {
   private final RSLVCore core;
   private final BaseServiceConfig config;
   private final OkHttpClient okHttpClient;
   private final String skinUrl;
   private final String skinModel;
   private final GameProfile profile;

   protected SkinRestorerFlows(RSLVCore core, BaseServiceConfig config, OkHttpClient okHttpClient, String skinUrl, String skinModel, GameProfile profile) {
      this.core = core;
      this.config = config;
      this.okHttpClient = okHttpClient;
      this.skinUrl = skinUrl;
      this.skinModel = skinModel;
      this.profile = profile;
   }

   public SkinRestorerResultImpl call() throws Exception {
      byte[] bytes;
      try {
         bytes = this.requireValidSkin(this.skinUrl, this.skinModel);
      } catch (Exception e) {
         return SkinRestorerResultImpl.ofBadSkin(e);
      }

      Request request;
      if (this.config.getSkinRestorer().getMethod() == SkinRestorerConfig.Method.UPLOAD) {
         request = new Builder()
            .url("https://api.mineskin.org/generate/upload")
            .header("User-Agent", "RSLB/1.1.1")
            .post(
               new okhttp3.MultipartBody.Builder()
                  .setType(MultipartBody.FORM)
                  .addFormDataPart("name", UUID.randomUUID().toString().substring(0, 6))
                  .addFormDataPart("variant", this.skinModel)
                  .addFormDataPart("visibility", "0")
                  .addFormDataPart("file", "upload.png", RequestBody.create(bytes, MediaType.parse("multipart/form-data")))
                  .build()
            )
            .build();
      } else {
         JsonObject jo = new JsonObject();
         jo.addProperty("name", UUID.randomUUID().toString().substring(0, 6));
         jo.addProperty("variant", this.skinModel);
         jo.addProperty("visibility", 0);
         jo.addProperty("url", this.skinUrl);
         request = new Builder()
            .url("https://api.mineskin.org/generate/url")
            .header("User-Agent", this.core.getHttpRequestHeaderUserAgent())
            .header("Content-Type", "application/json")
            .post(RequestBody.create(this.core.getGson().toJson(jo), MediaType.parse("application/json; charset=utf-8")))
            .build();
      }

      Response execute = this.okHttpClient.newCall(request).execute();
      JsonObject jo = JsonParser.parseString(Objects.requireNonNull(execute.body()).string())
         .getAsJsonObject()
         .getAsJsonObject("data")
         .getAsJsonObject("texture");
      String value = jo.getAsJsonPrimitive("value").getAsString();
      String signature = jo.getAsJsonPrimitive("signature").getAsString();

      try {
         this.core.getSqlManager().getSkinRestoredCacheTable().insertNew(ValueUtil.sha256(this.skinUrl), this.skinModel, value, signature);
      } catch (Exception e) {
         LoggerProvider.getLogger().warn("An exception occurred while saving restored skin data.", e);
      }

      Property restoredProperty = new Property();
      restoredProperty.setName("textures");
      restoredProperty.setValue(value);
      restoredProperty.setSignature(signature);
      this.profile.getPropertyMap().remove("textures");
      this.profile.getPropertyMap().put("textures", restoredProperty);
      return SkinRestorerResultImpl.ofRestorerSucceed(this.profile);
   }

   private byte[] requireValidSkin(String skinUrl, String model) throws IOException {
      Request request = new Builder().get().header("User-Agent", "RSLB/1.1.1").url(skinUrl).build();
      byte[] bytes = Objects.requireNonNull(this.okHttpClient.newCall(request).execute().body()).bytes();

      try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
         BufferedImage image = ImageIO.read(bais);
         boolean x64 = false;
         if (image.getWidth() != 64) {
            throw new SkinRestorerException("Skin width is not 64.");
         }

         if (image.getHeight() != 32 && image.getHeight() != 64) {
            throw new SkinRestorerException("Skin height is not 64 or 32.");
         }

         x64 = image.getHeight() == 64;
         return bytes;
      }
   }
}
