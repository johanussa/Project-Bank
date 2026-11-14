package org.uni.santo.tomas.model.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.uni.santo.tomas.helper.validator.ValidationGroups;

@Data
@Schema(description = "DTO para la creación y actualización de clientes")
public class CustomerDTO {

    @Schema(description = "Número de documento del cliente", examples = "1023456789", required = true)
    @NotBlank(message = "El número de documento no puede ser nulo o vacío", groups = ValidationGroups.Post.class)
    @Pattern(
            regexp = "^[0-9]{5,15}$", message = "El documento debe contener solo números entre 5 y 15 dígitos",
            groups = ValidationGroups.Post.class
    )
    private String documentNumber;

    @NotBlank(message = "El tipo de documento no puede ser nulo o vacío")
    @Schema(description = "Tipo de documento", examples = "CC", required = true)
    @Pattern(regexp = "^(CC|NIT|PASAPORTE)$", message = "El tipo de documento debe ser 'CC', 'NIT' o 'PASAPORTE'")
    private String documentType;

    @NotBlank(message = "Los nombres no pueden ser nulos o vacíos")
    @Size(max = 60, message = "Los nombres no pueden superar los 60 caracteres")
    @Schema(description = "Nombres del cliente", examples = "Juan Carlos", required = true)
    private String names;

    @NotBlank(message = "Los apellidos no pueden ser nulos o vacíos")
    @Size(max = 60, message = "Los apellidos no pueden superar los 60 caracteres")
    @Schema(description = "Apellidos del cliente", examples = "Rodriguez Torres", required = true)
    private String lastNames;

    @NotBlank(message = "El correo no puede ser nulo o vacío")
    @Email(message = "El correo ingresado tiene un formato inválido")
    @Size(max = 80, message = "El correo no puede superar los 80 caracteres")
    @Schema(description = "Correo del cliente", examples = "cliente@correo.com", required = true)
    private String email;

    @NotBlank(message = "El teléfono no puede ser nulo o vacío")
    @Size(max = 40, message = "El teléfono no puede superar los 40 caracteres")
    @Schema(description = "Número de teléfono", examples = "3102345678", required = true)
    private String phone;

    @Size(max = 100, message = "La dirección no puede superar los 100 caracteres")
    @Schema(description = "Dirección de residencia", examples = "Calle 72 # 12-03")
    private String address;
}
