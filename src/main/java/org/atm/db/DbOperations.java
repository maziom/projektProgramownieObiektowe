package org.atm.db;


import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.sql.SQLException;

public class DbOperations extends BaseDB {

    public static void updateClientCash(int clientId, BigDecimal cash) throws SQLException {
        var insertQuery = "UPDATE " + CLIENTS_TABLE_NAME + " SET " + "CASH=? WHERE CLIENT_ID=?";

        preparedStatement = conn.prepareStatement(insertQuery);
        preparedStatement.setBigDecimal(1, cash);
        preparedStatement.setInt(2, clientId);
        preparedStatement.executeUpdate();
    }
}
