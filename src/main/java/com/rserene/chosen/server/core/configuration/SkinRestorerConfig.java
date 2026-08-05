package com.rserene.chosen.server.core.configuration;

import lombok.Generated;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class SkinRestorerConfig {
   private final SkinRestorerConfig.RestorerType restorer;
   private final SkinRestorerConfig.Method method;
   private final int timeout;
   private final int retry;
   private final int retryDelay;
   private final ProxyConfig proxy;

   public static SkinRestorerConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
      SkinRestorerConfig.RestorerType restorer = (SkinRestorerConfig.RestorerType)((CommentedConfigurationNode)node.node(new Object[]{"restorer"}))
         .get(SkinRestorerConfig.RestorerType.class, SkinRestorerConfig.RestorerType.OFF);
      SkinRestorerConfig.Method method = (SkinRestorerConfig.Method)((CommentedConfigurationNode)node.node(new Object[]{"method"}))
         .get(SkinRestorerConfig.Method.class, SkinRestorerConfig.Method.URL);
      int timeout = ((CommentedConfigurationNode)node.node(new Object[]{"timeout"})).getInt(10000);
      int retry = ((CommentedConfigurationNode)node.node(new Object[]{"retry"})).getInt(2);
      int retryDelay = ((CommentedConfigurationNode)node.node(new Object[]{"retryDelay"})).getInt(5000);
      ProxyConfig proxy = ProxyConfig.read((CommentedConfigurationNode)node.node(new Object[]{"proxy"}));
      return new SkinRestorerConfig(restorer, method, timeout, retry, retryDelay, proxy);
   }

   @Generated
   private SkinRestorerConfig(
      SkinRestorerConfig.RestorerType restorer, SkinRestorerConfig.Method method, int timeout, int retry, int retryDelay, ProxyConfig proxy
   ) {
      this.restorer = restorer;
      this.method = method;
      this.timeout = timeout;
      this.retry = retry;
      this.retryDelay = retryDelay;
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
         + ", proxy="
         + this.getProxy()
         + ")";
   }

   public enum Method {
      URL,
      UPLOAD;
   }

   public enum RestorerType {
      OFF,
      LOGIN,
      ASYNC;
   }
}
