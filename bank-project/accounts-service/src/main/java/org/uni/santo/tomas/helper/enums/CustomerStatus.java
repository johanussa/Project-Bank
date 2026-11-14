package org.uni.santo.tomas.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomerStatus {

    ACTIVE("ACTIVO"),
    INACTIVE("INACTIVO");

    private final String status;
}
