package com.rserene.chosen.server.core.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.core.database.SQLManager;

public class SkinRestoredCacheTableV2 {
   private static final String fieldCurrentSkinUrlSha256 = "current_skin_url_sha256";
   private static final String fieldCurrentSkinModel = "current_skin_model";
   private static final String fieldRestorerValue = "restorer_value";
   private static final String fieldRestorerSignature = "restorer_signature";
   private final SQLManager sqlManager;
   private final String tableName;

   public SkinRestoredCacheTableV2(SQLManager sqlManager, String tableName) {
      this.sqlManager = sqlManager;
      this.tableName = tableName;
   }

   public void init(Connection connection) throws SQLException {
      String sql = MessageFormat.format(
         "CREATE TABLE IF NOT EXISTS {0} ( {1} BINARY(32) NOT NULL, {2} VARCHAR(16) NOT NULL, {3} LONGTEXT NOT NULL, {4} LONGTEXT NOT NULL, PRIMARY KEY ( {1}, {2} ))",
         this.tableName,
         "current_skin_url_sha256",
         "current_skin_model",
         "restorer_value",
         "restorer_signature"
      );

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
         preparedStatement.executeUpdate();
      }
   }

   public Pair<String, String> getCacheRestored(byte[] urlSha256, String model) throws SQLException {
      String sql = String.format(
         "SELECT %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1",
         "restorer_value",
         "restorer_signature",
         this.tableName,
         "current_skin_url_sha256",
         "current_skin_model"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, urlSha256);
         statement.setString(2, model);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return new Pair(resultSet.getString(1), resultSet.getString(2));
            }
         }
      }

      return null;
   }

   public void insertNew(byte[] urlSha256, String model, String value, String signature) throws SQLException {
      String sql = String.format(
         "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) ",
         this.tableName,
         "current_skin_url_sha256",
         "current_skin_model",
         "restorer_value",
         "restorer_signature"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, urlSha256);
         statement.setString(2, model);
         statement.setString(3, value);
         statement.setString(4, signature);
         statement.executeUpdate();
      }
   }
}
