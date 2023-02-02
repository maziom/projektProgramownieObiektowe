package org.atm;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.atm.db.DbOperations;

import java.math.BigDecimal;
import java.sql.SQLException;

public class MainTest extends TestCase {

    public void testWyplata() {
        Main main = new Main();
        main.main2ForTests(null);


        String nrKarty = "11111";
        final var card = DbOperations.getCardsLocally()
                .stream().filter(c -> c.getCardNumber().equals(nrKarty)).findFirst();

        var client = DbOperations.getClientsLocally()
                .stream().filter(cl -> cl.getClientId() == card.get().getClientId()).findFirst().get();
        //ustawiamy balans na 123
        client.setCash(new BigDecimal(123));

        //cash jest rowny 123 po ustawieniu go na 123
        Assert.assertEquals(client.getCash(), new BigDecimal(123));

        //zabieramy 123
        main.takeCash3(client, 123);


        client = DbOperations.getClientsLocally()
                .stream().filter(cl -> cl.getClientId() == card.get().getClientId()).findFirst().get();

        //cash jest rowny 0  po wyplacie kwoty 123
        Assert.assertEquals(0f, client.getCash().floatValue());

        DbOperations.closeConnection();
    }


    public void testDBConnection() throws SQLException {
        Main main = new Main();
        main.main2ForTests(null);

        Assert.assertTrue(DbOperations.conn.isValid(1000));
        DbOperations.closeConnection();
        Assert.assertFalse(DbOperations.conn.isValid(1000));
    }

    public void testStanKonta() {
        Main main = new Main();
        main.main2ForTests(null);


        String nrKarty = "11111";
        final var card = DbOperations.getCardsLocally()
                .stream().filter(c -> c.getCardNumber().equals(nrKarty)).findFirst();

        var client = DbOperations.getClientsLocally()
                .stream().filter(cl -> cl.getClientId() == card.get().getClientId()).findFirst().get();
        client.setCash(new BigDecimal(423.25f));
        Assert.assertEquals(client.getCash(), new BigDecimal(423.25f));
        DbOperations.closeConnection();
    }

}