package com.rserene.chosen.server.skinrestorer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import com.rserene.chosen.server.util.ValueUtil;
import com.rserene.chosen.server.profile.GameProfile;
import com.rserene.chosen.server.profile.Property;
import com.rserene.chosen.server.config.SkinRestorerConfig;
import com.rserene.chosen.server.config.service.BaseServiceConfig;
import com.rserene.chosen.server.main.RSLBCore;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class SkinRestorerFlows implements Callable<SkinRestorerResultImpl> {
   private static final String MINESKIN_ENDPOINT = "https://api.mineskin.org/v2/generate";
   private static final AtomicLong NEXT_REQUEST_AT = new AtomicLong();
   private static final String[] RETRYABLE_ERROR_CODES = new String[]{"rate_limit", "failed_to_create_id", "skin_change_failed"};
   private final RSLBCore core;
   private final BaseServiceConfig config;
   private final OkHttpClient okHttpClient;
   private final String skinUrl;
   private final String skinModel;
   private final GameProfile profile;

   protected SkinRestorerFlows(RSLBCore core, BaseServiceConfig config, OkHttpClient okHttpClient, String skinUrl, String skinModel, GameProfile profile) {
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

      Request request = this.buildRequest(bytes);
      SkinRestorerConfig skinRestorer = this.config.getSkinRestorer();
      int maxAttempts = Math.max(1, skinRestorer.getRetry() + 1);
      int attempt = 0;

      while (true) {
         long waitMills = NEXT_REQUEST_AT.get() - System.currentTimeMillis();
         if (waitMills > 0) {
            this.core.logDebug("Waiting " + waitMills + " ms before the next MineSkin request.");
            TimeUnit.MILLISECONDS.sleep(waitMills);
         }

         try (Response execute = this.okHttpClient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(execute.body()).string();
            MineSkinResponse mineSkinResponse = MineSkinResponse.parse(responseBody);
            this.updateNextRequestAt(mineSkinResponse);

            if (mineSkinResponse.isSuccess()) {
               return this.applyRestoredSkin(mineSkinResponse);
            }

            String errorCode = mineSkinResponse.getFirstErrorCode();
            if (errorCode == null && execute.code() == 429) {
               errorCode = "rate_limit";
            }

            if (errorCode == null && execute.code() == 401) {
               errorCode = "invalid_api_key";
            }

            if (errorCode != null && errorCode.equals("invalid_api_key")) {
               this.core.getLogger()
                  .warning(
                     "Skin restore failed: MineSkin rejected the API key used for " + this.profile.getName()
                        + ", please check the mineskinApiKey config."
                  );
               throw new SkinRestorerException("MineSkin generation failed: invalid API key.");
            }

            if (errorCode == null || !isRetryable(errorCode)) {
               if (errorCode != null) {
                  this.core.getLogger()
                     .warning(
                        "Skin restore failed: MineSkin rejected the skin with error '" + errorCode + "' for " + this.profile.getName() + "."
                     );
               }

               throw new SkinRestorerException("MineSkin generation failed: " + responseBody);
            }

            if (++attempt >= maxAttempts) {
               this.core.getLogger()
                  .warning("Skin restore failed: exhausted all " + maxAttempts + " attempts for " + this.profile.getName() + ".");
               throw new SkinRestorerException("MineSkin generation rate limited, exhausted all " + maxAttempts + " attempts.");
            }

            long retryWait = this.getRetryWaitMills(mineSkinResponse, skinRestorer);
            this.core.logDebug("MineSkin generation failed with '" + errorCode + "', retrying in " + retryWait + " ms.");
            TimeUnit.MILLISECONDS.sleep(Math.min(retryWait, (long)Math.max(skinRestorer.getTimeout(), 1)));
         }
      }
   }

   private Request buildRequest(byte[] bytes) {
      Builder builder;
      if (this.config.getSkinRestorer().getMethod() == SkinRestorerConfig.Method.UPLOAD) {
         builder = new Builder()
            .url(MINESKIN_ENDPOINT)
            .header("User-Agent", this.core.getHttpRequestHeaderUserAgent())
            .post(
               new MultipartBody.Builder()
                  .setType(MultipartBody.FORM)
                  .addFormDataPart("visibility", this.config.getSkinRestorer().getVisibility().name().toLowerCase())
                  .addFormDataPart("file", "skin.png", RequestBody.create(bytes, MediaType.parse("image/png")))
                  .build()
            );
      } else {
         JsonObject jo = new JsonObject();
         jo.addProperty("variant", this.skinModel);
         jo.addProperty("visibility", this.config.getSkinRestorer().getVisibility().name().toLowerCase());
         jo.addProperty("url", this.skinUrl);
         builder = new Builder()
            .url(MINESKIN_ENDPOINT)
            .header("User-Agent", this.core.getHttpRequestHeaderUserAgent())
            .header("Content-Type", "application/json")
            .post(RequestBody.create(this.core.getGson().toJson(jo), MediaType.parse("application/json; charset=utf-8")));
      }

      String apiKey = this.config.getSkinRestorer().getMineskinApiKey();
      if (apiKey != null && !apiKey.trim().isEmpty()) {
         builder.header("Authorization", "Bearer " + apiKey.trim());
      }

      return builder.build();
   }

   private SkinRestorerResultImpl applyRestoredSkin(MineSkinResponse mineSkinResponse) throws IOException {
      JsonObject textureData = mineSkinResponse.getTextureData();
      if (textureData == null) {
         throw new SkinRestorerException("MineSkin response does not contain texture data.");
      }

      String value = textureData.getAsJsonPrimitive("value").getAsString();
      String signature = textureData.getAsJsonPrimitive("signature").getAsString();

      try {
         this.core.getSqlManager().getSkinRestoredCacheTable().insertNew(ValueUtil.sha256(this.skinUrl), this.skinModel, value, signature);
      } catch (Exception e) {
         this.core.getLogger().log(java.util.logging.Level.WARNING, "An exception occurred while saving restored skin data.", e);
      }

      Property restoredProperty = new Property();
      restoredProperty.setName("textures");
      restoredProperty.setValue(value);
      restoredProperty.setSignature(signature);
      this.profile.getPropertyMap().remove("textures");
      this.profile.getPropertyMap().put("textures", restoredProperty);
      return SkinRestorerResultImpl.ofRestorerSucceed(this.profile);
   }

   private void updateNextRequestAt(MineSkinResponse mineSkinResponse) {
      long relative = mineSkinResponse.getRateLimitNextRelative();
      if (relative > 0) {
         NEXT_REQUEST_AT.updateAndGet(
            current -> Math.max(current, System.currentTimeMillis() + relative)
         );
      }
   }

   private long getRetryWaitMills(MineSkinResponse mineSkinResponse, SkinRestorerConfig skinRestorer) {
      long relative = mineSkinResponse.getRateLimitNextRelative();
      if (relative > 0) {
         return relative;
      }

      long delayMills = mineSkinResponse.getRateLimitDelayMills();
      if (delayMills > 0) {
         return delayMills;
      }

      return (long)skinRestorer.getRetryDelay();
   }

   private static boolean isRetryable(String errorCode) {
      for (String retryable : RETRYABLE_ERROR_CODES) {
         if (retryable.equals(errorCode)) {
            return true;
         }
      }

      return false;
   }

   private byte[] requireValidSkin(String skinUrl, String model) throws IOException {
      Request request = new Builder().get().header("User-Agent", this.core.getHttpRequestHeaderUserAgent()).url(skinUrl).build();
      byte[] bytes = Objects.requireNonNull(this.okHttpClient.newCall(request).execute().body()).bytes();
      BufferedImage image;
      try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
         image = ImageIO.read(bais);
      }

      if (image == null) {
         throw new SkinRestorerException("Skin image could not be read.");
      }

      if (image.getWidth() != 64) {
         throw new SkinRestorerException("Skin width is not 64.");
      }

      if (image.getHeight() != 32 && image.getHeight() != 64) {
         throw new SkinRestorerException("Skin height is not 64 or 32.");
      }

      return bytes;
   }

   private static class MineSkinResponse {
      private final boolean success;
      private final JsonObject root;

      private MineSkinResponse(boolean success, JsonObject root) {
         this.success = success;
         this.root = root;
      }

      public static MineSkinResponse parse(String responseBody) throws SkinRestorerException {
         JsonObject root;
         try {
            root = JsonParser.parseString(responseBody).getAsJsonObject();
         } catch (Exception e) {
            throw new SkinRestorerException("MineSkin returned a non-JSON response.", e);
         }

         boolean success = root.has("success") && root.getAsJsonPrimitive("success").getAsBoolean();
         return new MineSkinResponse(success, root);
      }

      public boolean isSuccess() {
         return this.success;
      }

      public String getFirstErrorCode() {
         if (!this.root.has("errors") || !this.root.get("errors").isJsonArray()) {
            return null;
         }

         JsonArray errors = this.root.getAsJsonArray("errors");
         if (errors.size() == 0) {
            return null;
         }

         JsonObject first = errors.get(0).getAsJsonObject();
         return first.has("code") ? first.getAsJsonPrimitive("code").getAsString() : null;
      }

      public JsonObject getTextureData() {
         JsonObject texture = this.getNestedObject(new String[]{"skin", "texture"});
         if (texture == null) {
            texture = this.getNestedObject(new String[]{"data", "texture"});
         }

         if (texture != null && texture.has("data") && texture.get("data").isJsonObject()) {
            return texture.getAsJsonObject("data");
         }

         if (texture != null && texture.has("value") && texture.has("signature")) {
            return texture;
         }

         return null;
      }

      private JsonObject getNestedObject(String[] path) {
         JsonElement current = this.root;
         for (int i = 0; i < path.length; i++) {
            if (current == null || !current.isJsonObject() || !current.getAsJsonObject().has(path[i])) {
               return null;
            }

            current = current.getAsJsonObject().get(path[i]);
         }

         return current != null && current.isJsonObject() ? current.getAsJsonObject() : null;
      }

      public long getRateLimitNextRelative() {
         JsonObject next = this.getRateLimitChild("next");
         if (next == null || !next.has("relative")) {
            return 0L;
         }

         JsonElement relative = next.get("relative");
         return relative.isJsonPrimitive() && relative.getAsJsonPrimitive().isNumber() ? relative.getAsLong() : 0L;
      }

      public long getRateLimitDelayMills() {
         JsonObject delay = this.getRateLimitChild("delay");
         if (delay == null || !delay.has("millis")) {
            return 0L;
         }

         JsonElement millis = delay.get("millis");
         return millis.isJsonPrimitive() && millis.getAsJsonPrimitive().isNumber() ? millis.getAsLong() : 0L;
      }

      private JsonObject getRateLimitChild(String child) {
         if (!this.root.has("rateLimit") || !this.root.get("rateLimit").isJsonObject()) {
            return null;
         }

         JsonObject rateLimit = this.root.getAsJsonObject("rateLimit");
         if (!rateLimit.has(child) || !rateLimit.get(child).isJsonObject()) {
            return null;
         }

         return rateLimit.getAsJsonObject(child);
      }
   }
}