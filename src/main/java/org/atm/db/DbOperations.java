package org.atm.db;


import lombok.SneakyThrows;

import java.math.BigDecimal;

public class DbOperations extends BaseDB {

    @SneakyThrows
    public static void updateClientCash(int clientId, BigDecimal cash) {
        var insertQuery = "UPDATE " + CLIENTS_TABLE_NAME + " SET " + "CASH=? WHERE CLIENT_ID=?";

        preparedStatement = conn.prepareStatement(insertQuery);
        preparedStatement.setBigDecimal(1, cash);
        preparedStatement.setInt(2, clientId);
        preparedStatement.executeUpdate();
    }
}
