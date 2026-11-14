package org.uni.santo.tomas.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {

    ACTIVE("ACTIVA"),
    LOCKED("BLOQUEADA"),
    CLOSED("CERRADA");

    private final String status;

}
