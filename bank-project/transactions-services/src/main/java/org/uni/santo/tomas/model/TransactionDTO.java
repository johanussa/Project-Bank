package org.uni.santo.tomas.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.uni.santo.tomas.helper.validator.ValidationGroups;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos requeridos para realizar un depósito")
public class TransactionDTO {

    @Pattern(
            regexp = "^[1-9]\\d{4,14}$",
            message = "El número de cuenta debe contener solo números entre 5 y 15 dígitos",
            groups = {ValidationGroups.Deposit.class, ValidationGroups.Transfer.class}
    )
    @NotBlank(
            message = "El número de cuenta destino no puede ser nulo o vacío",
            groups = {ValidationGroups.Deposit.class, ValidationGroups.Transfer.class}
    )
    @Schema(description = "Número de la cuenta destino del depósito", examples = "456789012", required = true)
    private String destinationAccountId;

    @Pattern(
            regexp = "^[1-9]\\d{4,14}$",
            message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos",
            groups = {ValidationGroups.Withdraw.class, ValidationGroups.Transfer.class}
    )
    @NotBlank(
            message = "El valor número de cuenta no puede ser nulo o vacío",
            groups = {ValidationGroups.Withdraw.class, ValidationGroups.Transfer.class}
    )
    @Schema(description = "ID de la cuenta de donde sale el dinero", examples = "456789012")
    private String originAccountId;

    @NotNull(message = "El monto del depósito no puede ser nulo")
    @Schema(description = "Monto a depositar", examples = "1870000", required = true)
    @DecimalMin(value = "0.01", message = "El monto del depósito debe ser mayor a cero")
    @Digits(integer = 15, fraction = 2, message = "El monto debe tener hasta 15 enteros y 2 decimales")
    private BigDecimal amount;
}
