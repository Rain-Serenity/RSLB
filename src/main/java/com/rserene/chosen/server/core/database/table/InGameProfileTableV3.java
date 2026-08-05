package com.rserene.chosen.server.core.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.Pair;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.database.SQLManager;

public class InGameProfileTableV3 {
   private static final String fieldInGameUuid = "in_game_uuid";
   private static final String fieldCurrentUsernameLowerCase = "current_username_lower_case";
   private static final String fieldCurrentUsernameOriginal = "current_username_original";
   private final String tableName;
   private final String tableNameV2;
   private final SQLManager sqlManager;

   public InGameProfileTableV3(SQLManager sqlManager, String tableName, String tableNameV2) {
      this.tableName = tableName;
      this.sqlManager = sqlManager;
      this.tableNameV2 = tableNameV2;
   }

   public void init(Connection connection) throws SQLException {
      String sql = MessageFormat.format(
         "CREATE TABLE IF NOT EXISTS {0} ( {1} BINARY(16) NOT NULL, {2} VARCHAR(64) DEFAULT NULL, {3} VARCHAR(64) DEFAULT NULL, CONSTRAINT IGPT_V3_PR PRIMARY KEY ( {1} ), CONSTRAINT IGPT_V3_UN UNIQUE ( {2} ))",
         this.tableName,
         "in_game_uuid",
         "current_username_lower_case",
         "current_username_original"
      );

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
         preparedStatement.executeUpdate();

         try (
            PreparedStatement prepareStatement = connection.prepareStatement("SELECT COUNT(0) FROM " + this.tableName);
            ResultSet resultSet = prepareStatement.executeQuery();
         ) {
            resultSet.next();
            if (resultSet.getInt(1) != 0) {
               return;
            }
         }

         try (
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(0) FROM " + this.tableNameV2);
            ResultSet resultSet = statement.executeQuery();
         ) {
            resultSet.next();
            if (resultSet.getInt(1) == 0) {
               return;
            }
         } catch (Exception ignored) {
            return;
         }
      }

      LoggerProvider.getLogger().info("Updating in game profile data...");
      List<Pair<byte[], String>> oldData = new ArrayList<>();

      try (
         PreparedStatement statement = connection.prepareStatement("SELECT in_game_uuid, current_username FROM " + this.tableNameV2);
         ResultSet resultSet = statement.executeQuery();
      ) {
         while (resultSet.next()) {
            oldData.add(new Pair(resultSet.getBytes(1), resultSet.getString(2)));
         }
      }

      for (Pair<byte[], String> datum : oldData) {
         try (PreparedStatement statement = connection.prepareStatement(
               String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)", this.tableName, "in_game_uuid", "current_username_lower_case")
            )) {
            statement.setBytes(1, (byte[])datum.getValue1());
            statement.setString(2, Optional.ofNullable((String)datum.getValue2()).map(String::toLowerCase).orElse(null));
            statement.executeUpdate();
         }
      }

      LoggerProvider.getLogger().info("Updated in game profile data, total " + oldData.size() + ".");
   }

   public Pair<UUID, String> get(UUID inGameUUID) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", "current_username_original", this.tableName, "in_game_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               String string = resultSet.getString(1);
               return new Pair(inGameUUID, string);
            }
         }
      }

      return null;
   }

   public UUID getInGameUUIDIgnoreCase(String currentUsername) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE LOWER(%s) = ? LIMIT 1", "in_game_uuid", this.tableName, "current_username_lower_case");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, currentUsername.toLowerCase(Locale.ROOT));

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return ValueUtil.bytesToUuid(resultSet.getBytes(1));
            }
         }
      }

      return null;
   }

   public boolean dataExists(UUID inGameUUID) throws SQLException {
      String sql = String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1", this.tableName, "in_game_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
         }
      }
   }

   public String getUsername(UUID inGameUUID) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", "current_username_original", this.tableName, "in_game_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return resultSet.getString(1);
            }
         }
      }

      return null;
   }

   public void updateUsername(UUID inGameUUID, String currentUsername) throws SQLException {
      String sql = String.format(
         "UPDATE %s SET %s = ?, %s = ? WHERE %s = ?", this.tableName, "current_username_lower_case", "current_username_original", "in_game_uuid"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, currentUsername.toLowerCase(Locale.ROOT));
         statement.setString(2, currentUsername);
         statement.setBytes(3, ValueUtil.uuidToBytes(inGameUUID));
         statement.executeUpdate();
      }
   }

   public void insertNewData(UUID inGameUUID, String currentUsername) throws SQLException {
      String sql = String.format(
         "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)", this.tableName, "in_game_uuid", "current_username_lower_case", "current_username_original"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         connection.setAutoCommit(false);
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));
         statement.setString(2, currentUsername.toLowerCase());
         statement.setString(3, currentUsername);
         statement.executeUpdate();
         connection.commit();
      }
   }

   public boolean remove(UUID uuid) throws SQLException {
      String sql = String.format("DELETE FROM %s WHERE %s = ?", this.tableName, "in_game_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(uuid));
         return statement.executeUpdate() == 1;
      }
   }

   public int eraseUsername(String currentUsername) throws SQLException {
      String sql = String.format(
         "UPDATE %s SET %s = ?, %s = ? WHERE LOWER(%s) = ?",
         this.tableName,
         "current_username_lower_case",
         "current_username_original",
         "current_username_lower_case"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, null);
         statement.setString(2, null);
         statement.setString(3, currentUsername.toLowerCase(Locale.ROOT));
         return statement.executeUpdate();
      }
   }

   public int eraseAllUsername() throws SQLException {
      String sql = String.format("UPDATE %s SET %s = ?, %s = ?", this.tableName, "current_username_lower_case", "current_username_original");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, null);
         statement.setString(2, null);
         return statement.executeUpdate();
      }
   }
}
