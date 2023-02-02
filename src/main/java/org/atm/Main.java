package org.atm;

import org.atm.db.DbOperations;
import org.atm.objects.Card;
import org.atm.objects.Client;
import org.atm.objects.CreditCard;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

import static org.atm.objects.CreditCard.*;

public class Main {

    public static void main(String[] args) {
        DbOperations.initDB();

        System.out.println("|-------------Witaj---------------|\n");
        startView();

        DbOperations.closeConnection();
    }

    public static void main2ForTests(String[] args) {
        DbOperations.initDB();
//        startView();
//        DbOperations.closeConnection();
    }

    private static void startView() {
        System.out.println("Dostępne operacje:");
        System.out.println("1.Uruchom bankomat");
        System.out.println("2.Ustaw dostępność kart dla klienta");
        System.out.println("3.Zakończ");
        System.out.println("\nWybierz opcje i zatwierdź klawiszem ENTER:");

        startOptions();
    }

    private static void startOptions() {
        switch (getIntValue(3)) {
            case 1 -> startATM();
            case 2 -> setCardsAccess();
            case 3 -> exit();
        }
    }

    private static void startATM() {
        System.out.println("\nDostępne operacje:");
        System.out.println("1.Zaloguj sie na konto");
        System.out.println("2.Zakończ");
        System.out.println("\nWybierz opcje i zatwierdź klawiszem ENTER:");

        switch (getIntValue(2)) {
            case 1 -> atmOptions();
            case 2 -> exit();
        }
    }

    private static void atmOptions() {
        System.out.println("\nWpisz nr karty kredytowej i naciśnij ENTER");
        final var scanner = new Scanner(System.in);
        final var value = scanner.next();
        final var stringBuilder = new StringBuilder();

        try {
            Integer.parseInt(value);
            if (value.length() < 7) {
                stringBuilder.append("\nWITAJ\n");
            } else {

            }

        } catch (NumberFormatException e) {
            System.out.println("Wpisany nr karty jest niepoprawny, zostajesz przeniesiony na początkowy ekran bankomatu");
            startATM();
        }

        final var card = DbOperations.getCardsLocally()
                .stream().filter(c -> c.getCardNumber().equals(value)).findFirst();

        if (card.isPresent()) {
            final var client = DbOperations.getClientsLocally()
                    .stream().filter(cl -> cl.getClientId() == card.get().getClientId()).findFirst().get();

            stringBuilder.append(client.getFirstName()).append(" ").append(client.getLastName());
            stringBuilder.append(" używasz karty ").append(card.get().getName());
            System.out.println(stringBuilder);

            if (card.get().isActive()) {
                enterPin(card.get(), client);
            } else {
                System.out.println("Niestety twoja karta nie jest obsługiwana, zostajesz przeniesiony na ekran startowy bankomatu");
                startATM();
            }
        } else {
            System.out.println("Wpisany nr karty jest przypisany do żadnego konta, zostajesz przeniesiony na początkowy ekran bankomatu");
            startATM();
        }
    }

    private static void enterPin(Card card, Client client) {
        System.out.println("\nWpisz PIN i naciśnij ENTER");
        final var scanner = new Scanner(System.in);

        var attempts = 3;
        while (attempts > 0) {
            if (card.getPinNumber().equals(scanner.next())) {
                System.out.println("Zostałeś zalogowany");
                loggedClient(client);
            } else if (attempts == 1) {
                System.out.println("Trzy razy wpisałeś niepoprawny pin, zostajesz przeniesiony na ekran główny bankomatu");
                startATM();
            } else {
                attempts--;
                System.out.println("Wpisany PIN jest niepoprawny, spróbuj ponownie zostało ci " + attempts + " prób");
            }
        }
    }

    private static void loggedClient(Client client) {
        System.out.println("\nDostępne operacje:");
        System.out.println("1.Wypłać pieniądze");
        System.out.println("2.Sprawdź stan konta");
        System.out.println("3.Wyloguj się z konta");
        System.out.println("4.Zakończ");
        System.out.println("\nWybierz opcje i zatwierdź klawiszem ENTER:");

        switch (getIntValue(4)) {
            case 1 -> takeCash(client);
            case 2 -> checkCash(client);
            case 3 -> startATM();
            case 4 -> exit();
        }
    }

    public static void takeCash3(Client client, int value) {
        final var restMoney = client.getCash().subtract(new BigDecimal(value));
        try {
            DbOperations.updateClientCash(client.getClientId(), restMoney);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void takeCash(Client client, int value) {
        try {

            if (value % 10 > 0) {
                System.out.println("Twoja liczba nie jest poprawna, pamiętaj że musi być podzielna przez 10");
                takeCash(client);
            } else if (value > client.getCash().intValue()) {
                System.out.println("Nie masz wystarczających środków na koncie");
                takeCash(client);
            } else {
                takeCash3(client, value);
                System.out.println("Wybrałeś ze swojego konta " + value);
                System.out.println("Zostajesz wylogowany i przeniesiony do ekranu startowego");
                startATM();
            }
        } catch (InputMismatchException exception) {
            System.out.println("Wpisana wartość jest niepoprawna, ponieważ zawiera litery, spróbuj ponownie");
            takeCash(client);
        }
    }

    public static void takeCash(Client client) {
        System.out.println("Wprowadź kwotę podzielną przez 10");
        final var scanner = new Scanner(System.in);
        final var value = scanner.nextInt();
        takeCash(client, value);

    }

    private static void checkCash(Client client) {
        System.out.println("Twój stan konta:");
        System.out.println(client.getCash());
        loggedClient(client);
    }

    private static void setCardsAccess() {
        final var clients = DbOperations.getClientsLocally();

        clients.forEach(client ->
                System.out.println(client.getClientId() + "." + client.getFirstName() + " " + client.getLastName())
        );

        final var clientId = getIntValue(clients.size());

        setCardAccessForClient(clientId, VISA);
        setCardAccessForClient(clientId, VISA_ELECTRON);
        setCardAccessForClient(clientId, AMERICAN_EXPRESS);
        setCardAccessForClient(clientId, MASTERCARD);

        startView();
    }

    private static void setCardAccessForClient(int clientId, CreditCard creditCard) {
        System.out.println(creditCard + "  - czy klient może korzystać z karty?");
        System.out.println("1.TAK");
        System.out.println("2.NIE");
        final var option = getIntValue(2);

        final var access = option == 1;

        DbOperations.setAccessibilityOfCard(clientId, creditCard, access);
    }

    private static int getIntValue(int limit) {
        final var scanner = new Scanner(System.in);
        try {
            final var value = scanner.nextInt();

            if (value <= limit && value != 0) {
                return value;
            } else {
                System.out.println("Wprowadź wartość z zakresu 1-" + limit);
                return getIntValue(limit);
            }
        } catch (Exception e) {
            System.out.println("Wprowadź poprawna wartość -> musi być to liczba");
            return getIntValue(limit);
        }
    }

    private static void exit() {
        System.out.println("|--------Dziekujemy !!!--------|");
        System.out.println("|-----Zapraszamy ponownie------|");
        System.exit(0);
    }
}