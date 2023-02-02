package org.atm.db;

import lombok.SneakyThrows;
import org.atm.FilesHelper;
import org.atm.objects.Card;
import org.atm.objects.Client;
import org.atm.objects.CreditCard;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.atm.objects.CreditCard.*;

public abstract class BaseDB {
    protected static final String CLIENTS_TABLE_NAME = "CLIENTS";
    protected static final String CARDS_TABLE_NAME = "CARDS";
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:~/test";
    private static final String USER = "sa";
    private static final String PASS = "";
    public static Connection conn;
    protected static PreparedStatement preparedStatement;
    protected static Statement stmt;

    @SneakyThrows
    public static void initDB() {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        stmt = conn.createStatement();
        dropTableIfExist(CARDS_TABLE_NAME);
        dropTableIfExist(CLIENTS_TABLE_NAME);
        createTables();
        fillTableExampleData();
    }

    @SneakyThrows
    public static void closeConnection() {
        stmt.close();
        conn.close();
    }

    @SneakyThrows
    public static List<Client> getClientsLocally() {
        final var clients = new ArrayList<Client>();

        preparedStatement = conn.prepareStatement("SELECT * FROM " + CLIENTS_TABLE_NAME);

        final var resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            final var client = Client.builder()
                    .clientId(resultSet.getInt("CLIENT_ID"))
                    .firstName(resultSet.getString("FIRSTNAME"))
                    .lastName(resultSet.getString("LASTNAME"))
                    .cash(resultSet.getBigDecimal("CASH"))
                    .build();

            clients.add(client);
        }

        return clients;
    }

    @SneakyThrows
    public static List<Card> getCardsLocally() {
        final var cards = new ArrayList<Card>();

        preparedStatement = conn.prepareStatement("SELECT * FROM " + CARDS_TABLE_NAME);

        final var resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            final var card = Card.builder()
                    .cardId(resultSet.getInt("CARD_ID"))
                    .pinNumber(resultSet.getString("PIN_NUMBER"))
                    .name(CreditCard.valueOf(resultSet.getString("NAME")))
                    .cardNumber(resultSet.getString("CARD_NUMBER"))
                    .clientId(resultSet.getInt("CLIENT_ID"))
                    .active(resultSet.getBoolean("ACTIVE"))
                    .build();

            cards.add(card);
        }

        return cards;
    }

    @SneakyThrows
    public static void setAccessibilityOfCard(int clientId, CreditCard creditCard, boolean accessibility) {
        var insertQuery = "UPDATE " + CARDS_TABLE_NAME + " SET " + "ACTIVE=? WHERE CLIENT_ID=? AND NAME=?";

        preparedStatement = conn.prepareStatement(insertQuery);
        preparedStatement.setBoolean(1, accessibility);
        preparedStatement.setInt(2, clientId);
        preparedStatement.setString(3, creditCard.name());
        preparedStatement.executeUpdate();
    }

    @SneakyThrows
    private static void createTables() {
        createClientsTable();
        createCardsTable();
    }

    private static void dropTableIfExist(String tableName) {
        final var createTableSQL = "DROP TABLE IF EXISTS " + tableName;

        try {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println("Query can't be executed");
        }
    }

    @SneakyThrows
    private static void createClientsTable() {
        final var createTableSQL = "CREATE TABLE " + CLIENTS_TABLE_NAME +
                " (client_id IDENTITY NOT NULL PRIMARY KEY ," +
                " firstName VARCHAR(255), " +
                " lastName VARCHAR(255), " +
                " cash DECIMAL(15,2), " + " PRIMARY KEY ( client_id ))";

        stmt.execute(createTableSQL);
        System.out.println("Created CLIENTS table");
    }

    @SneakyThrows
    private static void createCardsTable() {
        final var createTableSQL = "CREATE TABLE " + CARDS_TABLE_NAME +
                " (card_id IDENTITY NOT NULL PRIMARY KEY ," +
                " pin_number VARCHAR(4) not null," +
                " name VARCHAR not null," +
                " card_number VARCHAR(10) not null," +
                " client_id INTEGER not null," +
                " active BOOLEAN not null," +
                " constraint client_id" +
                " foreign key (client_id) references CLIENTS (CLIENT_ID))";

        stmt.execute(createTableSQL);
        System.out.println("Created CARDS table");
    }

    @SneakyThrows
    public static void fillTableExampleData() {
        final var data = FilesHelper.getFile("accounts.csv");

        final var clients = new ArrayList<Client>();
        data.lines().forEach(line -> {
            final var tab = line.split(";");

            final var tempClient = Client.builder()
                    .clientId(Integer.parseInt(tab[0]))
                    .firstName(tab[1])
                    .lastName(tab[2])
                    .cash(new BigDecimal(tab[3]))
                    .visa(Card.builder()
                            .pinNumber(tab[4])
                            .active(Boolean.parseBoolean(tab[5]))
                            .cardNumber(tab[6])
                            .name(VISA)
                            .build())
                    .americanExpress(Card.builder()
                            .pinNumber(tab[7])
                            .active(Boolean.parseBoolean(tab[8]))
                            .cardNumber(tab[9])
                            .name(AMERICAN_EXPRESS)
                            .build())
                    .visaElectron(Card.builder()
                            .pinNumber(tab[10])
                            .active(Boolean.parseBoolean(tab[11]))
                            .cardNumber(tab[12])
                            .name(VISA_ELECTRON)
                            .build())
                    .mastercard(Card.builder()
                            .pinNumber(tab[13])
                            .active(Boolean.parseBoolean(tab[14]))
                            .cardNumber(tab[15])
                            .name(MASTERCARD)
                            .build())
                    .build();

            clients.add(tempClient);
        });

        clients.forEach(client -> {
            var insertQuery = "INSERT INTO " + CLIENTS_TABLE_NAME + "(FIRSTNAME, LASTNAME, CASH) VALUES (?,?,?)";
            try {
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, client.getFirstName());
                preparedStatement.setString(2, client.getLastName());
                preparedStatement.setBigDecimal(3, client.getCash());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            final var clientId = getClientsLocally().stream()
                    .filter(c -> c.getFirstName().equals(client.getFirstName()) && c.getLastName().equals(client.getLastName()))
                    .findFirst().get().getClientId();
            addCard(client.getVisa(), clientId);
            addCard(client.getVisaElectron(), clientId);
            addCard(client.getAmericanExpress(), clientId);
            addCard(client.getMastercard(), clientId);

        });

        System.out.println("Added example data...");
    }

    private static void addCard(Card card, int clientId) {
        var insertQuery = "INSERT INTO " + CARDS_TABLE_NAME + "(PIN_NUMBER,NAME,CLIENT_ID,ACTIVE,CARD_NUMBER) VALUES (?,?,?,?,?)";
        try {
            preparedStatement = conn.prepareStatement(insertQuery);
            preparedStatement.setString(1, card.getPinNumber());
            preparedStatement.setString(2, card.getName().name());
            preparedStatement.setInt(3, clientId);
            preparedStatement.setBoolean(4, card.isActive());
            preparedStatement.setString(5, card.getCardNumber());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
