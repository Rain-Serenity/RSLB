package com.rserene.chosen.server.core.configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ProxyConfig {
   private final Type type;
   private final String hostname;
   private final int port;
   private final String username;
   private final String password;

   public static ProxyConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
      Type type = (Type)((CommentedConfigurationNode)node.node(new Object[]{"type"})).get(Type.class, Type.DIRECT);
      String hostname = ((CommentedConfigurationNode)node.node(new Object[]{"hostname"})).getString("127.0.0.1");
      int port = ((CommentedConfigurationNode)node.node(new Object[]{"port"})).getInt(1080);
      String username = ((CommentedConfigurationNode)node.node(new Object[]{"username"})).getString("");
      String password = ((CommentedConfigurationNode)node.node(new Object[]{"password"})).getString("");
      return new ProxyConfig(type, hostname, port, username, password);
   }

   public Proxy getProxy() {
      return this.type == Type.DIRECT ? Proxy.NO_PROXY : new Proxy(this.type, new InetSocketAddress(this.hostname, this.port));
   }

   public Authenticator getProxyAuthenticator() {
      return (route, response) -> {
         if (ValueUtil.isEmpty(this.username)) {
            return null;
         }

         String credential = Credentials.basic(this.username, this.password);
         return response.request().newBuilder().header("Proxy-Authorization", credential).build();
      };
   }

   @Generated
   public ProxyConfig(Type type, String hostname, int port, String username, String password) {
      this.type = type;
      this.hostname = hostname;
      this.port = port;
      this.username = username;
      this.password = password;
   }

   @Generated
   public Type getType() {
      return this.type;
   }

   @Generated
   public String getHostname() {
      return this.hostname;
   }

   @Generated
   public int getPort() {
      return this.port;
   }

   @Generated
   public String getUsername() {
      return this.username;
   }

   @Generated
   public String getPassword() {
      return this.password;
   }

   @Generated
   @Override
   public String toString() {
      return "ProxyConfig(type="
         + this.getType()
         + ", hostname="
         + this.getHostname()
         + ", port="
         + this.getPort()
         + ", username="
         + this.getUsername()
         + ", password="
         + this.getPassword()
         + ")";
   }
}
