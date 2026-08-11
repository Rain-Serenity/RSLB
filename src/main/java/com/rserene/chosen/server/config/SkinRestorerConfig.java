package com.rserene.chosen.server.config;

import lombok.Generated;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class SkinRestorerConfig {
   private final SkinRestorerConfig.RestorerType restorer;
   private final SkinRestorerConfig.Method method;
   private final int timeout;
   private final int retry;
   private final int retryDelay;
   private final String mineskinApiKey;
   private final SkinRestorerConfig.Visibility visibility;
   private final ProxyConfig proxy;

   public static SkinRestorerConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
      SkinRestorerConfig.RestorerType restorer = readRestorer((CommentedConfigurationNode)node.node(new Object[]{"restorer"}));
      SkinRestorerConfig.Method method = (SkinRestorerConfig.Method)((CommentedConfigurationNode)node.node(new Object[]{"method"}))
         .get(SkinRestorerConfig.Method.class, SkinRestorerConfig.Method.URL);
      int timeout = ((CommentedConfigurationNode)node.node(new Object[]{"timeout"})).getInt(10000);
      int retry = ((CommentedConfigurationNode)node.node(new Object[]{"retry"})).getInt(2);
      int retryDelay = ((CommentedConfigurationNode)node.node(new Object[]{"retryDelay"})).getInt(5000);
      String mineskinApiKey = ((CommentedConfigurationNode)node.node(new Object[]{"mineskinApiKey"})).getString("");
      SkinRestorerConfig.Visibility visibility = (SkinRestorerConfig.Visibility)((CommentedConfigurationNode)node.node(new Object[]{"visibility"}))
         .get(SkinRestorerConfig.Visibility.class, SkinRestorerConfig.Visibility.PUBLIC);
      ProxyConfig proxy = ProxyConfig.read((CommentedConfigurationNode)node.node(new Object[]{"proxy"}));
      return new SkinRestorerConfig(restorer, method, timeout, retry, retryDelay, mineskinApiKey, visibility, proxy);
   }

   private static SkinRestorerConfig.RestorerType readRestorer(CommentedConfigurationNode node) {
      String value = node.getString("OFF");
      if (value == null || value.trim().equalsIgnoreCase("OFF")) {
         return SkinRestorerConfig.RestorerType.OFF;
      }

      if (value.trim().equalsIgnoreCase("LOGIN")) {
         return SkinRestorerConfig.RestorerType.LOGIN;
      }

      if (value.trim().equalsIgnoreCase("ASYNC")) {
         try {
            ((CommentedConfigurationNode)node.node(new Object[]{"restorer"})).set("LOGIN");
         } catch (SerializationException ignored) {
         }

         return SkinRestorerConfig.RestorerType.LOGIN;
      }

      return SkinRestorerConfig.RestorerType.OFF;
   }

   @Generated
   private SkinRestorerConfig(
      SkinRestorerConfig.RestorerType restorer, SkinRestorerConfig.Method method, int timeout, int retry, int retryDelay, String mineskinApiKey, SkinRestorerConfig.Visibility visibility, ProxyConfig proxy
   ) {
      this.restorer = restorer;
      this.method = method;
      this.timeout = timeout;
      this.retry = retry;
      this.retryDelay = retryDelay;
      this.mineskinApiKey = mineskinApiKey;
      this.visibility = visibility;
      this.proxy = proxy;
   }

   @Generated
   public SkinRestorerConfig.RestorerType getRestorer() {
      return this.restorer;
   }

   @Generated
   public SkinRestorerConfig.Method getMethod() {
      return this.method;
   }

   @Generated
   public int getTimeout() {
      return this.timeout;
   }

   @Generated
   public int getRetry() {
      return this.retry;
   }

   @Generated
   public int getRetryDelay() {
      return this.retryDelay;
   }

   @Generated
   public String getMineskinApiKey() {
      return this.mineskinApiKey;
   }

   @Generated
   public SkinRestorerConfig.Visibility getVisibility() {
      return this.visibility;
   }

   @Generated
   public ProxyConfig getProxy() {
      return this.proxy;
   }

   @Generated
   @Override
   public String toString() {
      return "SkinRestorerConfig(restorer="
         + this.getRestorer()
         + ", method="
         + this.getMethod()
         + ", timeout="
         + this.getTimeout()
         + ", retry="
         + this.getRetry()
         + ", retryDelay="
         + this.getRetryDelay()
         + ", mineskinApiKey="
         + this.getMineskinApiKey()
         + ", visibility="
         + this.getVisibility()
         + ", proxy="
         + this.getProxy()
         + ")";
   }

   public enum Method {
      URL,
      UPLOAD;
   }

   public enum Visibility {
      PUBLIC,
      UNLISTED,
      PRIVATE;
   }

   public enum RestorerType {
      OFF,
      LOGIN;
   }
}