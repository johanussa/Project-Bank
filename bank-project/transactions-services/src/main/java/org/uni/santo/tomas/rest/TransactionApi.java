package org.uni.santo.tomas.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.helper.exception.HandlerException;
import org.uni.santo.tomas.helper.exception.ProblemException;
import org.uni.santo.tomas.helper.validator.ValidationGroups;
import org.uni.santo.tomas.model.Transaction;
import org.uni.santo.tomas.model.TransactionDTO;
import org.uni.santo.tomas.service.TransactionService;

import java.util.List;

@Path("/api/v1/transactions-service")
@OpenAPIDefinition(
        info = @Info(title = "Bank API", version = "1.0"),
        security = @SecurityRequirement(name = "ApiKeyAuth")
)
@SecurityScheme(
        securitySchemeName = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        apiKeyName = "x-api-key",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "Transacciones-API")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "ApiKeyAuth")
public class TransactionApi {

    private static final Logger LOG = Logger.getLogger(TransactionApi.class.getName());

    @Inject
    TransactionService transactionService;

    @GET
    @Path("/{referenceNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "La transacción se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Transaction.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en el parámetro suministrado",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de referencia no puede ser nulo o vacío"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Usuario no autorizado para realizar esta solicitud",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, examples = """
                            {
                              "title": "Unauthorized",
                              "detail": "La API Key es inválida o esta ausente"
                            }"""))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La transacción con la referencia ingresada no existe",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Not Found"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "La transacción con referencia TX-20231027-001 no existe"
                            )
                    }))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Obtener una transacción por número de referencia",
            description = "Retorna el detalle de una transacción específica según su número de referencia"
    )
    public Response getTransaction(
            @Parameter(
                    description = "Número de referencia de la transacción",
                    example = "TX-20261110-001",
                    required = true
            )
            @NotBlank(message = "El número de referencia no puede ser nulo o vacío")
            @PathParam("referenceNumber") String referenceNumber
    ) {

        LOG.infof("@getTransaction API > Inicia API del servicio para obtener la transaccion con numero de " +
                "referencia: %s", referenceNumber);

        Transaction transaction = transactionService.getTransactionById(referenceNumber);

        LOG.info("@getTransaction API > Finaliza API, la transaccion se obtuvo correctamente");

        return Response.ok().entity(transaction).build();
    }

    @GET
    @Path("/account/{accountNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El historial de transacciones se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Transaction.class, type = SchemaType.ARRAY))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en el parámetro suministrado",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El valor número de cuenta no puede ser nulo o vacío",
                                        "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Usuario no autorizado para realizar esta solicitud",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, examples = """
                            {
                              "title": "Unauthorized",
                              "detail": "La API Key es inválida o esta ausente"
                            }"""))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Obtener historial de transacciones por cuenta",
            description = "Retorna el listado de transacciones donde la cuenta fue origen o destino, ordenadas de " +
                    "la más reciente a la más antigua"
    )
    public Response getTransactionsByAccount(
            @Parameter(
                    description = "Número de cuenta a consultar el historial",
                    example = "456789012",
                    required = true
            )
            @NotBlank(message = "El valor número de cuenta no puede ser nulo o vacío")
            @Pattern(regexp = "^[1-9]\\d{4,14}$", message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos")
            @PathParam("accountNumber") String accountNumber
    ) {

        LOG.infof("@getTransactionsByAccount API > Inicia API del servicio para obtener el listado de " +
                "transacciones por el numero de la cuenta: %s", accountNumber);

        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountNumber);

        LOG.info("@getTransactionsByAccount API > Finaliza API de servicio para obtener listado de transacciones");

        return Response.ok().entity(transactions).build();
    }

    @POST
    @Path("/deposit")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "El depósito se realizó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados o la cuenta no existe/está inactiva",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de cuenta destino no puede ser nulo o vacío",
                                        "El monto del depósito debe ser mayor a cero",
                                        "Debe ingresar la información de la transacción con el deposito a realizar"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La cuenta destino no existe",
                    content = @Content(schema = @Schema(implementation = ProblemException.class))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Realizar un depósito",
            description = "Registra un depósito en una cuenta activa y actualiza su saldo"
    )
    public Response depositAmount(
            @RequestBody(
                    description = "Datos del depósito a realizar",
                    content = @Content(schema = @Schema(implementation = TransactionDTO.class), example = """
                            {
                                "destinationAccountId": "456789012",
                                "amount": 1870000
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.Deposit.class)
            @NotNull(message = "Debe ingresar la información de la transacción con el deposito a realizar")
            @Valid TransactionDTO transaction
    ) {

        LOG.infof("@depositAmount API > Inicia API del servicio para depositar dinero en la cuenta: %s",
                transaction.getDestinationAccountId());

        transactionService.depositAmount(transaction);

        LOG.info("@depositAmount API > Finaliza API del servicio de deposito de dinero");

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/withdraw")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "El retiro se realizó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados o fondos insuficientes",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de cuenta origen no puede ser nulo o vacío",
                                        "El monto del retiro debe ser mayor a cero",
                                        "Fondos insuficientes para realizar el retiro"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La cuenta origen no existe o no está activa",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Not Found"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "La cuenta con el numero de cuenta ingresado no existe"
                            )
                    }))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Realizar un retiro",
            description = "Retira un monto de una cuenta activa si tiene saldo suficiente"
    )
    public Response withdrawAmount(
            @RequestBody(
                    description = "Datos del retiro a realizar",
                    content = @Content(schema = @Schema(implementation = TransactionDTO.class), example = """
                            {
                                "originAccountId": "456789012",
                                "amount": 1870000
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.Withdraw.class)
            @NotNull(message = "Debe ingresar la información de la transacción con el retiro a realizar")
            @Valid TransactionDTO transaction
    ) {

        LOG.infof("@withdrawAmount API > Inicia API del servicio para retirar un monto de dinero de la " +
                "cuenta: %s", transaction.getOriginAccountId());

        transactionService.withdrawAmount(transaction);

        LOG.info("@withdrawAmount API > Finaliza API del servicio de retiro de dinero");

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/transfer")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "La transferencia se realizó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos, cuentas iguales o fondos insuficientes",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de cuenta origen no puede ser nulo o vacío",
                                        "La cuenta origen y destino no pueden ser la misma",
                                        "Fondos insuficientes para realizar la transferencia"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La cuenta origen o destino no existe o no está activa",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Not Found"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "La cuenta con el numero de cuenta ingresado no existe"
                            )
                    }))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Fallo en la actualización de la cuenta destino. El dinero fue devuelto a la cuenta origen",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Internal Server Error"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "Fallo al depositar en la cuenta destino. El dinero ha sido devuelto a su cuenta origen"
                            )
                    }))
            )
    })
    @Operation(
            summary = "Realizar una transferencia entre cuentas",
            description = "Transfiere un monto de una cuenta origen a una cuenta destino, validando saldo y " +
                    "aplicando compensación si falla el depósito en destino"
    )
    public Response transferAmount(
            @RequestBody(
                    description = "Datos de la transferencia a realizar",
                    content = @Content(schema = @Schema(implementation = TransactionDTO.class), example = """
                            {
                                "destinationAccountId": "456789013",
                                "originAccountId": "456789012",
                                "amount": 1870000
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.Transfer.class)
            @NotNull(message = "Debe ingresar la información de la transferencia a realizar")
            @Valid TransactionDTO transaction
    ) {

        LOG.infof("@transferAmount API > Inicia API del servicio para realizar transferencia de dinero con " +
                "los datos: %s", transaction);

        transactionService.transferAmount(transaction);

        LOG.info("@transferAmount API > Finaliza API del servicio de transferencia de dinero");

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
