package org.uni.santo.tomas.model.account;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.uni.santo.tomas.helper.validator.ValidationGroups;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO para la creación y actualización de cuentas")
public class AccountDTO {

    @Schema(description = "Número de la cuenta del cliente", examples = "456789012", required = true)
    @NotBlank(
            message = "El valor número de cuenta no puede ser nulo o vacío",
            groups = {ValidationGroups.Put.class, ValidationGroups.Post.class, ValidationGroups.PutBalance.class}
    )
    @Pattern(
            regexp = "^[1-9]\\d{4,14}$",
            message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos",
            groups = {ValidationGroups.Put.class, ValidationGroups.Post.class, ValidationGroups.PutBalance.class}
    )
    private String accountNumber;

    @NotBlank(message = "El tipo de cuenta no puede ser nulo o vacío")
    @Schema(description = "Tipo de cuenta del usuario", examples = {"AHORROS", "CORRIENTE"}, required = true)
    @Pattern(regexp = "^(AHORROS|CORRIENTE)$", message = "El tipo de cuenta debe ser 'AHORROS' o 'CORRIENTE'")
    @Null(message = "El valor del campo tipo de cuenta debe ser nulo", groups = ValidationGroups.PutBalance.class)
    private String accountType;

    @Schema(description = "Saldo de la cuenta del cliente", examples = "1850000")
    @DecimalMin(
            value = "0.00", message = "El saldo de la cuenta no puede ser negativo",
            groups = {ValidationGroups.Post.class, ValidationGroups.PutBalance.class}
    )
    @Digits(
            integer = 15, fraction = 2, message = "El saldo de la cuenta debe tener hasta 15 enteros y 2 decimales",
            groups = {ValidationGroups.Post.class, ValidationGroups.PutBalance.class}
    )
    @NotNull(message = "El valor del campo saldo no puede ser nulo", groups = ValidationGroups.PutBalance.class)
    @Null(message = "Para esta actualización No debe enviar el campo saldo", groups = ValidationGroups.Put.class)
    private BigDecimal balance;

    @NotBlank(message = "El tipo de moneda no puede ser nulo o vacío")
    @Pattern(regexp = "^(COP|USD|EUR)$", message = "La moneda debe ser 'COP', 'USD' o 'EUR'")
    @Null(message = "El valor del campo tipo de moneda debe ser nulo", groups = ValidationGroups.PutBalance.class)
    @Schema(description = "Tipo de moneda de la cuenta del cliente", examples = {"COP", "USD", "EUR"}, required = true)
    private String currency;

    @Null(
            message = "El valor del campo estado debe ser nulo",
            groups = {ValidationGroups.Post.class, ValidationGroups.PutBalance.class}
    )
    @Schema(description = "Estado de la cuenta del cliente", examples = {"ACTIVA", "BLOQUEADA", "CERRADA"})
    @NotNull(message = "El valor del campo estado no debe ser nulo o vacío", groups = ValidationGroups.Put.class)
    @Pattern(
            regexp = "^(ACTIVA|BLOQUEADA|CERRADA)$",
            message = "El valor del campo estado debe ser 'BLOQUEADA' o 'CERRADA'",
            groups = ValidationGroups.Put.class
    )
    private String status;

    @Null(
            message = "El valor del campo fecha registro debe ser nulo",
            groups = {ValidationGroups.Post.class, ValidationGroups.Put.class, ValidationGroups.PutBalance.class}
    )
    @Schema(description = "Fecha de la apertura de la cuenta del cliente", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime openingDate;

    @Schema(examples = "1013456789", required = true)
    @NotBlank(message = "El número de documento del cliente no puede ser nulo o vacío")
    @Null(message = "El valor del campo número de documento debe ser nulo", groups = ValidationGroups.PutBalance.class)
    private String customerDocumentNumber;
}
