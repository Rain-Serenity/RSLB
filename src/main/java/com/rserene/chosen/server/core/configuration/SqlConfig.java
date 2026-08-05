package com.rserene.chosen.server.core.configuration;

import lombok.Generated;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class SqlConfig {
   private final SqlConfig.SqlBackend backend;
   private final String ip;
   private final int port;
   private final String username;
   private final String password;
   private final String database;
   private final String tablePrefix;
   private final String connectUrl;

   public static SqlConfig read(CommentedConfigurationNode node) throws SerializationException {
      SqlConfig.SqlBackend backend = (SqlConfig.SqlBackend)((CommentedConfigurationNode)node.node(new Object[]{"backend"}))
         .get(SqlConfig.SqlBackend.class, SqlConfig.SqlBackend.H2);
      String ip = ((CommentedConfigurationNode)node.node(new Object[]{"host"})).getString("127.0.0.1");
      int port = ((CommentedConfigurationNode)node.node(new Object[]{"port"})).getInt(3306);
      String username = ((CommentedConfigurationNode)node.node(new Object[]{"username"})).getString("root");
      String password = ((CommentedConfigurationNode)node.node(new Object[]{"password"})).getString("root");
      String database = ((CommentedConfigurationNode)node.node(new Object[]{"database"})).getString("rslb");
      String tablePrefix = ((CommentedConfigurationNode)node.node(new Object[]{"table-prefix"})).getString("rslb");
      String connectUrl = ((CommentedConfigurationNode)node.node(new Object[]{"connect-url"})).getString("");
      return new SqlConfig(backend, ip, port, username, password, database, tablePrefix, connectUrl);
   }

   @Generated
   private SqlConfig(
      SqlConfig.SqlBackend backend, String ip, int port, String username, String password, String database, String tablePrefix, String connectUrl
   ) {
      this.backend = backend;
      this.ip = ip;
      this.port = port;
      this.username = username;
      this.password = password;
      this.database = database;
      this.tablePrefix = tablePrefix;
      this.connectUrl = connectUrl;
   }

   @Generated
   public SqlConfig.SqlBackend getBackend() {
      return this.backend;
   }

   @Generated
   public String getIp() {
      return this.ip;
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
   public String getDatabase() {
      return this.database;
   }

   @Generated
   public String getTablePrefix() {
      return this.tablePrefix;
   }

   @Generated
   public String getConnectUrl() {
      return this.connectUrl;
   }

   @Generated
   @Override
   public String toString() {
      return "SqlConfig(backend="
         + this.getBackend()
         + ", ip="
         + this.getIp()
         + ", port="
         + this.getPort()
         + ", username="
         + this.getUsername()
         + ", password="
         + this.getPassword()
         + ", database="
         + this.getDatabase()
         + ", tablePrefix="
         + this.getTablePrefix()
         + ", connectUrl="
         + this.getConnectUrl()
         + ")";
   }

   public enum SqlBackend {
      H2,
      MYSQL;
   }
}
