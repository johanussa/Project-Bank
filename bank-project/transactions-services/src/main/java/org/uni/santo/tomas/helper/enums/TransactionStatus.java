package org.uni.santo.tomas.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatus {

    PENDING("PENDIENTE"),
    COMPLETED("COMPLETADA"),
    FAILED("FALLIDA");

    private final String status;
}
