package org.uni.santo.tomas.model;

import lombok.Builder;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Account {

    @Schema(description = "Número de la cuenta del cliente", examples = "456789012", required = true)
    private String accountNumber;

    @Schema(description = "Tipo de cuenta del usuario", examples = {"AHORROS", "CORRIENTE"}, required = true)
    private String accountType;

    @Schema(description = "Saldo de la cuenta del cliente", examples = "1850000")
    private BigDecimal balance;

    @Schema(description = "Tipo de moneda de la cuenta del cliente", examples = {"COP", "USD"}, required = true)
    private String currency;

    @Schema(description = "Estado de la cuenta del cliente", examples = {"ACTIVA", "BLOQUEADA", "CERRADA"})
    private String status;

    @Schema(description = "Fecha de la apertura de la cuenta del cliente", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime openingDate;

    private Customer customer;
}
