package com.rserene.chosen.server.database.pool;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;

public class H2ConnectionPool implements ISQLConnectionPool {
   public static final String defaultUrl = "jdbc:h2:{0};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
   private final JdbcConnectionPool cp;

   public H2ConnectionPool(File dataFolder, String user, String password) throws ClassNotFoundException {
      this(dataFolder, user, password, "jdbc:h2:{0};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0");
   }

   public H2ConnectionPool(File dataFolder, String user, String password, String url) throws ClassNotFoundException {
      Class.forName("org.h2.Driver");
      this.cp = JdbcConnectionPool.create(url.replace("{0}", dataFolder.getAbsolutePath() + File.separator + "RSLB"), user, password);
   }

   @Override
   public Connection getConnection() throws SQLException {
      return this.cp.getConnection();
   }

   @Override
   public String name() {
      return "H2";
   }

   @Override
   public void close() {
      this.cp.dispose();
   }
}
