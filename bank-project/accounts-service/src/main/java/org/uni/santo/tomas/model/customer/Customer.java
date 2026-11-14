package org.uni.santo.tomas.model.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "CLIENTE")
public class Customer {

    @Id
    @Column(name = "NUMERO_DOCUMENTO", length = 15, nullable = false)
    @Schema(description = "Numero de identificación del cliente", examples = "1023456789", required = true)
    private String documentNumber;

    @Column(name = "TIPO_DOCUMENTO", length = 10, nullable = false)
    @Schema(description = "Tipo de identificación del cliente", examples = {"CC", "NIT", "Pasaporte"}, required = true)
    private String documentType;

    @Column(name = "NOMBRES", length = 60, nullable = false)
    @Schema(description = "Nombres del cliente", examples = "Juan Carlos", required = true)
    private String names;

    @Column(name = "APELLIDOS", length = 60, nullable = false)
    @Schema(description = "Apellidos del cliente", examples = "Rodriguez Torres", required = true)
    private String lastNames;

    @Column(name = "CORREO", length = 80, nullable = false)
    @Schema(description = "Correo del cliente", examples = "cliente@correo.com", required = true)
    private String email;

    @Column(name = "TELEFONO", length = 40, nullable = false)
    @Schema(description = "Número de teléfono del cliente", examples = "3102345678", required = true)
    private String phone;

    @Column(name = "DIRECCION", length = 100)
    @Schema(description = "Dirección de residencia del cliente", examples = "Calle 72 # 12-03")
    private String address;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    @Schema(description = "Fecha en la que se registro el cliente", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime dateRegistration;

    @Column(name = "ESTADO", length = 10)
    @Schema(description = "Estado del cliente", examples = {"ACTIVO", "INACTIVO"})
    private String status;
}
