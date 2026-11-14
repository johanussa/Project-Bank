package org.uni.santo.tomas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "TRANSACCION")
public class Transaction {

    @Id
    @Column(name = "NUMERO_REFERENCIA", length = 60, nullable = false)
    @Schema(description = "Código único de referencia de transacción", examples = "TX-20231027-001")
    private String referenceNumber;

    @Column(name = "TIPO")
    @Schema(description = "Tipo de transacción", examples = {"DEPOSITO", "RETIRO", "TRANSFERENCIA"})
    private String type;

    @Column(name = "MONTO")
    @Schema(description = "Valor de la transacción", examples = "1870000")
    @Positive(message = "El valor del monto de la transacción debe ser mayor a 0")
    private BigDecimal amount;

    @Column(name = "CUENTA_ORIGEN", length = 60)
    @Schema(description = "ID de la cuenta de donde sale el dinero", examples = "456789012")
    private String originAccountId;

    @Column(name = "CUENTA_DESTINO", length = 60)
    @Schema(description = "ID de la cuenta de donde llega el dinero", examples = "456789012")
    private String destinationAccountId;

    @Column(name = "FECHA")
    @Schema(description = "Fecha de la transacción", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime transactionDate;

    @Column(name = "ESTADO", length = 60, nullable = false)
    @Schema(description = "Estado de transacción", examples = {"PENDIENTE", "COMPLETADA", "FALLIDA"})
    private String status;

    @Column(name = "DESCRIPCION")
    @Schema(description = "Motivo de la transacción", examples = "Pago de nómina")
    private String description;
}
