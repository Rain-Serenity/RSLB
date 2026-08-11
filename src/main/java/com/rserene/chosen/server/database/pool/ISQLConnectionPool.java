package com.rserene.chosen.server.database.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ISQLConnectionPool {
   Connection getConnection() throws SQLException;

   String name();

   void close();
}
