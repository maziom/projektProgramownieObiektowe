package org.atm.objects;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class Client {
    private int clientId;
    private String firstName;
    private String lastName;
    private Card visa;
    private Card visaElectron;
    private Card americanExpress;
    private Card mastercard;
    private BigDecimal cash;

}
