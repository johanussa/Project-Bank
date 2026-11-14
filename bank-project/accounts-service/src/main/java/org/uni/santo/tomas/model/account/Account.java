package org.uni.santo.tomas.model.account;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.uni.santo.tomas.model.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "CUENTA")
public class Account {

    @Id
    @Column(name = "NUMERO_CUENTA", length = 20, nullable = false, unique = true)
    @Schema(description = "Número de la cuenta del cliente", examples = "456789012", required = true)
    private String accountNumber;

    @Column(name = "TIPO_CUENTA", length = 50, nullable = false)
    @Schema(description = "Tipo de cuenta del usuario", examples = {"AHORROS", "CORRIENTE"}, required = true)
    private String accountType;

    @Column(name = "SALDO")
    @Schema(description = "Saldo de la cuenta del cliente", examples = "1850000")
    private BigDecimal balance;

    @Column(name = "MONEDA", length = 80, nullable = false)
    @Schema(description = "Tipo de moneda de la cuenta del cliente", examples = {"COP", "USD", "EUR"}, required = true)
    private String currency;

    @Column(name = "ESTADO", length = 20, nullable = false)
    @Schema(description = "Estado de la cuenta del cliente", examples = {"ACTIVA", "BLOQUEADA", "CERRADA"})
    private String status;

    @Column(name = "FECHA_APERTURA")
    @Schema(description = "Fecha de la apertura de la cuenta del cliente", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime openingDate;

    @ManyToOne
    @JoinColumn(name = "NUMERO_DOCUMENTO")
    private Customer customer;
}
