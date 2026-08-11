package com.rserene.chosen.server.database;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.Generated;
import com.rserene.chosen.server.util.ValueUtil;
import com.rserene.chosen.server.config.SqlConfig;
import com.rserene.chosen.server.database.pool.H2ConnectionPool;
import com.rserene.chosen.server.database.pool.ISQLConnectionPool;
import com.rserene.chosen.server.database.pool.MysqlConnectionPool;
import com.rserene.chosen.server.database.table.InGameProfileTableV3;
import com.rserene.chosen.server.database.table.SkinRestoredCacheTableV2;
import com.rserene.chosen.server.database.table.UserDataTableV3;
import com.rserene.chosen.server.main.RSLBCore;

public class SQLManager {
   private final RSLBCore core;
   private ISQLConnectionPool pool;
   private InGameProfileTableV3 inGameProfileTable;
   private UserDataTableV3 userDataTable;
   private SkinRestoredCacheTableV2 skinRestoredCacheTable;

   public SQLManager(RSLBCore core) {
      this.core = core;
   }

   @Generated
   public RSLBCore getCore() {
      return this.core;
   }

   public void init() throws SQLException, ClassNotFoundException {
      SqlConfig sqlConfig = this.core.getPluginConfig().getSqlConfig();
      if (sqlConfig.getBackend() == SqlConfig.SqlBackend.MYSQL) {
         this.pool = new MysqlConnectionPool(
            sqlConfig.getIp(),
            sqlConfig.getPort(),
            sqlConfig.getDatabase(),
            sqlConfig.getUsername(),
            sqlConfig.getPassword(),
            ValueUtil.isEmpty(sqlConfig.getConnectUrl())
               ? "jdbc:mysql://{0}:{1}/{2}?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false"
               : sqlConfig.getConnectUrl()
         );
      } else {
         if (sqlConfig.getBackend() != SqlConfig.SqlBackend.H2) {
            throw new UnsupportedOperationException("Database type Unknown.");
         }

         this.pool = new H2ConnectionPool(
            this.core.getPlugin().getDataFolder(),
            sqlConfig.getUsername(),
            sqlConfig.getPassword(),
            ValueUtil.isEmpty(sqlConfig.getConnectUrl()) ? "jdbc:h2:{0};TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0" : sqlConfig.getConnectUrl()
         );
      }

      String tablePrefix = sqlConfig.getTablePrefix() + "_";
      String inGameProfileTableNameV2 = tablePrefix + "in_game_profile_v2";
      String inGameProfileTableNameV3 = tablePrefix + "in_game_profile_v3";
      String userDataTableNameV2 = tablePrefix + "user_data_v2";
      String userDataTableNameV3 = tablePrefix + "user_data_v3";
      String skinRestorerCacheTableNameV2 = tablePrefix + "skin_restored_cache_v2";
      this.userDataTable = new UserDataTableV3(this, userDataTableNameV3, userDataTableNameV2);
      this.skinRestoredCacheTable = new SkinRestoredCacheTableV2(this, skinRestorerCacheTableNameV2);
      this.inGameProfileTable = new InGameProfileTableV3(this, inGameProfileTableNameV3, inGameProfileTableNameV2);

      try (Connection connection = this.getPool().getConnection()) {
         connection.setAutoCommit(false);
         this.userDataTable.init(connection);
         this.inGameProfileTable.init(connection);
         this.skinRestoredCacheTable.init(connection);
         connection.commit();
      }
   }

   public void close() {
      if (this.pool != null) {
         this.pool.close();
      }
   }

   @Generated
   public ISQLConnectionPool getPool() {
      return this.pool;
   }

   @Generated
   public InGameProfileTableV3 getInGameProfileTable() {
      return this.inGameProfileTable;
   }

   @Generated
   public UserDataTableV3 getUserDataTable() {
      return this.userDataTable;
   }

   @Generated
   public SkinRestoredCacheTableV2 getSkinRestoredCacheTable() {
      return this.skinRestoredCacheTable;
   }
}
