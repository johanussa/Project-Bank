package org.uni.santo.tomas.model;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
public class Customer {

    @Schema(description = "Numero de identificación del cliente", examples = "1023456789", required = true)
    private String documentNumber;

    @Schema(description = "Tipo de identificación del cliente", examples = {"CC", "NIT", "Pasaporte"}, required = true)
    private String documentType;

    @Schema(description = "Nombres del cliente", examples = "Juan Carlos", required = true)
    private String names;

    @Schema(description = "Apellidos del cliente", examples = "Rodriguez Torres", required = true)
    private String lastNames;

    @Schema(description = "Correo del cliente", examples = "cliente@correo.com", required = true)
    private String email;

    @Schema(description = "Número de teléfono del cliente", examples = "3102345678", required = true)
    private String phone;

    @Schema(description = "Dirección de residencia del cliente", examples = "Calle 72 # 12-03")
    private String address;

    @Schema(description = "Fecha en la que se registro el cliente", examples = "2026-11-08T21:48:03.449605")
    private LocalDateTime dateRegistration;

    @Schema(description = "Estado del cliente", examples = {"ACTIVO", "INACTIVO"})
    private String status;
}
