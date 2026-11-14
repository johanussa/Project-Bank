package org.uni.santo.tomas.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    DEPOSIT("DEPOSITO"),
    WITHDRAWAL("RETIRO"),
    TRANSFER("TRANSFERENCIA");

    private final String type;
}
