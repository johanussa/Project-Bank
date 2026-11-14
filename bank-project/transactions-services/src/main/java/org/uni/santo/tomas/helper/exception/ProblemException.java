package org.uni.santo.tomas.helper.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemException {

    @Schema(examples = "localhost:8081")
    private String host;

    @Schema(examples = "Solicitud inválida")
    private String title;

    @Schema(examples = "Error en la solicitud enviada")
    private String detail;

    @Schema(description = "URI (Identificador Uniforme de Recursos) categoría general que identifica a un recurso",
            examples = "http://localhost:8081/accounts-service/customers/1234567890")
    private String uri;

    @Schema(examples = "El valor del campo documentNumber no puede ser nulo o vacío", type = SchemaType.ARRAY)
    private List<String> errors;
}
