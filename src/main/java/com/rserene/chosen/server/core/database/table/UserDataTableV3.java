package com.rserene.chosen.server.core.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.logger.LoggerProvider;
import com.rserene.chosen.server.api.internal.util.There;
import com.rserene.chosen.server.api.internal.util.ValueUtil;
import com.rserene.chosen.server.core.command.CommandHandler;
import com.rserene.chosen.server.core.configuration.service.BaseServiceConfig;
import com.rserene.chosen.server.core.database.SQLManager;

public class UserDataTableV3 {
   private static final String fieldOnlineUUID = "online_uuid";
   private static final String fieldOnlineName = "online_name";
   private static final String fieldServiceId = "service_id";
   private static final String fieldInGameProfileUuid = "in_game_profile_uuid";
   private static final String fieldWhitelist = "whitelist";
   private final SQLManager sqlManager;
   private final String tableName;
   private final String tableNameV2;

   public UserDataTableV3(SQLManager sqlManager, String tableName, String tableNameV2) {
      this.sqlManager = sqlManager;
      this.tableName = tableName;
      this.tableNameV2 = tableNameV2;
   }

   public void init(Connection connection) throws SQLException {
      String sql = MessageFormat.format(
         "CREATE TABLE IF NOT EXISTS {0} ( {1} BINARY(16) NOT NULL, {2} INTEGER NOT NULL, {3} VARCHAR(64) DEFAULT NULL, {4} BINARY(16) DEFAULT NULL, {5} BOOL DEFAULT FALSE, PRIMARY KEY ( {1}, {2} ))",
         this.tableName,
         "online_uuid",
         "service_id",
         "online_name",
         "in_game_profile_uuid",
         "whitelist"
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

      LoggerProvider.getLogger().info("Updating user data...");

      class V2Entry {
         private final byte[] onlineUUID;
         private final int serviceId;
         private final byte[] inGameProfileUUID;
         private final boolean whitelist;

         @Generated
         public V2Entry(final byte[] onlineUUID, final int serviceId, final byte[] inGameProfileUUID, final boolean whitelist) {
            this.onlineUUID = onlineUUID;
            this.serviceId = serviceId;
            this.inGameProfileUUID = inGameProfileUUID;
            this.whitelist = whitelist;
         }
      }

      List<V2Entry> oldData = new ArrayList<>();

      try (
         PreparedStatement statement = connection.prepareStatement("SELECT online_uuid, yggdrasil_id, in_game_profile_uuid, whitelist FROM " + this.tableNameV2);
         ResultSet resultSet = statement.executeQuery();
      ) {
         while (resultSet.next()) {
            oldData.add(new V2Entry(resultSet.getBytes(1), resultSet.getBytes(2)[0], resultSet.getBytes(3), resultSet.getBoolean(4)));
         }
      }

      for (V2Entry datum : oldData) {
         try (PreparedStatement statement = connection.prepareStatement(
               String.format(
                  "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)", this.tableName, "online_uuid", "service_id", "in_game_profile_uuid", "whitelist"
               )
            )) {
            statement.setBytes(1, datum.onlineUUID);
            statement.setInt(2, datum.serviceId);
            statement.setBytes(3, datum.inGameProfileUUID);
            statement.setBoolean(4, datum.whitelist);
            statement.executeUpdate();
         }
      }

      LoggerProvider.getLogger().info("Updated user data, total " + oldData.size() + ".");
   }

   public There<String, UUID, Boolean> get(UUID onlineUUID, int serviceId) throws SQLException {
      String sql = String.format(
         "SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1",
         "online_name",
         "in_game_profile_uuid",
         "whitelist",
         this.tableName,
         "online_uuid",
         "service_id"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return new There(
                  resultSet.getString(1), (UUID)Optional.ofNullable(resultSet.getBytes(2)).map(ValueUtil::bytesToUuid).orElse(null), resultSet.getBoolean(3)
               );
            }
         }
      }

      return null;
   }

   public UUID getOnlineUUID(String username, int serviceId) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE lower(%s) = ? AND %s = ? LIMIT 1", "online_uuid", this.tableName, "online_name", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, username.toLowerCase(Locale.ROOT));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return Optional.ofNullable(resultSet.getBytes(1)).<UUID>map(ValueUtil::bytesToUuid).orElse(null);
            }
         }
      }

      return null;
   }

   public UUID getInGameUUID(UUID onlineUUID, int serviceId) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1", "in_game_profile_uuid", this.tableName, "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return Optional.ofNullable(resultSet.getBytes(1)).<UUID>map(ValueUtil::bytesToUuid).orElse(null);
            }
         }
      }

      return null;
   }

   public Set<Integer> getOnlineServiceIds(UUID inGameUUID) throws SQLException {
      Set<Integer> result = new HashSet<>();
      String sql = String.format("SELECT %s FROM %s WHERE %s = ?", "service_id", this.tableName, "in_game_profile_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
               result.add(resultSet.getInt(1));
            }
         }
      }

      return Collections.unmodifiableSet(result);
   }

   public Set<There<UUID, String, Integer>> getOnlineProfiles(UUID inGameUUID) throws SQLException {
      Set<There<UUID, String, Integer>> result = new HashSet<>();
      String sql = String.format("SELECT %s, %s, %s FROM %s WHERE %s = ?", "online_uuid", "online_name", "service_id", this.tableName, "in_game_profile_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
               result.add(
                  new There(
                     (UUID)Optional.ofNullable(resultSet.getBytes(1)).map(ValueUtil::bytesToUuid).orElse(null), resultSet.getString(2), resultSet.getInt(3)
                  )
               );
            }
         }
      }

      return Collections.unmodifiableSet(result);
   }

   public int setInGameUUID(UUID onlineUUID, int serviceId, UUID newInGameUUID) throws SQLException {
      String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1", this.tableName, "in_game_profile_uuid", "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(newInGameUUID));
         statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(3, serviceId);
         return statement.executeUpdate();
      }
   }

   public boolean dataExists(UUID onlineUUID, int serviceId) throws SQLException {
      String sql = String.format("SELECT 1 FROM %s WHERE %s = ? AND %s = ? LIMIT 1", this.tableName, "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
         }
      }
   }

   public int insertNewData(UUID onlineUUID, int serviceId, String onlineName, UUID inGameUUID) throws SQLException {
      String sql = String.format(
         "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) ", this.tableName, "online_uuid", "service_id", "online_name", "in_game_profile_uuid"
      );

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);
         statement.setString(3, onlineName);
         if (inGameUUID == null) {
            statement.setNull(4, -2);
         } else {
            statement.setBytes(4, ValueUtil.uuidToBytes(inGameUUID));
         }

         return statement.executeUpdate();
      }
   }

   public void setWhitelist(UUID onlineUUID, int serviceId, boolean whitelist) throws SQLException {
      String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1", this.tableName, "whitelist", "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBoolean(1, whitelist);
         statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(3, serviceId);
         statement.executeUpdate();
      }
   }

   public boolean hasWhitelist(UUID onlineUUID, int serviceId) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1", "whitelist", this.tableName, "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return resultSet.getBoolean(1);
            }
         }
      }

      return false;
   }

   public boolean hasWhitelist(UUID inGameUUID) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", "whitelist", this.tableName, "in_game_profile_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(inGameUUID));

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return resultSet.getBoolean(1);
            }
         }
      }

      return false;
   }

   public void setWhitelist(UUID inGameUUID, boolean whitelist) throws SQLException {
      String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?LIMIT 1", this.tableName, "whitelist", "in_game_profile_uuid");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBoolean(1, whitelist);
         statement.setBytes(2, ValueUtil.uuidToBytes(inGameUUID));
         statement.executeUpdate();
      }
   }

   public List<String> listWhitelist(boolean verbose) throws SQLException {
      String sql = verbose
         ? String.format(
            "SELECT %s, %s, %s, %s FROM %s WHERE %s = true", "online_name", "service_id", "online_uuid", "in_game_profile_uuid", this.tableName, "whitelist"
         )
         : String.format("SELECT %s FROM %s WHERE %s = true", "online_name", this.tableName, "whitelist");
      List<String> result = new ArrayList<>();

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery();
      ) {
         if (verbose) {
            while (resultSet.next()) {
               int serviceId = resultSet.getInt(2);
               BaseServiceConfig serviceConfig = CommandHandler.getCore().getPluginConfig().getServiceIdMap().get(serviceId);
               String serviceName = serviceConfig == null
                  ? CommandHandler.getCore().getLanguageHandler().getMessage("command_message_find_profile_entry_unused_service")
                  : serviceConfig.getName();
               result.add(
                  String.format(
                     "%s (%s=%d(%s), %s=%s, %s=%s)",
                     resultSet.getString(1),
                     "service_id",
                     serviceId,
                     serviceName,
                     "online_uuid",
                     ValueUtil.bytesToUuid(resultSet.getBytes(3)),
                     "in_game_profile_uuid",
                     Optional.ofNullable(resultSet.getBytes(4)).map(ValueUtil::bytesToUuid).orElse(null)
                  )
               );
            }
         } else {
            while (resultSet.next()) {
               result.add(String.format("%s", resultSet.getString(1)));
            }
         }
      }

      return result;
   }

   public void setOnlineName(UUID onlineUUID, int serviceId, String onlineName) throws SQLException {
      String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? LIMIT 1", this.tableName, "online_name", "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setString(1, onlineName);
         statement.setBytes(2, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(3, serviceId);
         statement.executeUpdate();
      }
   }

   public String getOnlineName(UUID onlineUUID, int serviceId) throws SQLException {
      String sql = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1", "online_name", this.tableName, "online_uuid", "service_id");

      try (
         Connection connection = this.sqlManager.getPool().getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
      ) {
         statement.setBytes(1, ValueUtil.uuidToBytes(onlineUUID));
         statement.setInt(2, serviceId);

         try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
               return resultSet.getString(1);
            }
         }

         return null;
      }
   }
}
