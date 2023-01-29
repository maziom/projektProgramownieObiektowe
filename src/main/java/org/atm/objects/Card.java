package org.atm.objects;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Card {
    private int cardId;
    private int clientId;
    private String pinNumber;
    private String cardNumber;
    private CreditCard name;
    private boolean active;
}
