package org.uni.santo.tomas.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.uni.santo.tomas.model.account.Account;
import org.uni.santo.tomas.model.account.AccountDTO;
import org.uni.santo.tomas.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Cuentas-API")
@Path("/api/v1/accounts-service/accounts")
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
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "ApiKeyAuth")
public class AccountApi {

    private static final Logger LOG = Logger.getLogger(AccountApi.class.getName());

    @Inject
    AccountService accountService;

    @GET
    @Path("/customer/{documentNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El listado de cuentas se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Account.class, type = SchemaType.ARRAY))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los parámetros suministrados",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de documento del cliente no puede ser nulo o vacío",
                                        "El número de documento debe contener solo números entre 5 y 15 dígitos"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Obtener cuentas por cliente",
            description = "Retorna el listado de cuentas activas asociadas a un cliente"
    )
    public Response getAccountsByCustomer(
            @Parameter(
                    description = "Número de documento del cliente a consultar",
                    example = "1023456789",
                    required = true
            )
            @NotBlank(message = "El número de documento del cliente no puede ser nulo o vacío")
            @Pattern(regexp = "^[0-9]{5,15}$", message = "El número de documento debe contener solo números entre 5 y 15 dígitos")
            @PathParam("documentNumber") String documentNumber
    ) {

        LOG.infof("@getAccountsByCustomer API > Inicia API de servicio para obtener cuentas del cliente con documento: %s",
                documentNumber);

        List<Account> accounts = accountService.getAccountsByCustomer(documentNumber);

        LOG.info("@getAccountsByCustomer API > Finaliza API de servicio para obtener cuentas del cliente");

        return Response.ok().entity(accounts).build();
    }

    @GET
    @Path("/{accountNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "La cuenta se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Account.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los recursos suministrados",
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
                    responseCode = "404",
                    description = "La cuenta con el numero ingresado no existe",
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
                    description = "Error interno de servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Obtener cuenta por numero",
            description = "Retorna la información referente a la cuenta consultada por el número de esta"
    )
    public Response getAccountByNumber(
            @Parameter(
                    description = "Número de la cuenta a obtener",
                    example = "456789012",
                    required = true
            )
            @NotBlank(message = "El valor número de cuenta no puede ser nulo o vacío")
            @Pattern(regexp = "^[1-9]\\d{4,14}$", message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos")
            @PathParam("accountNumber") String accountNumber
    ) {

        LOG.infof("@getAccountByNumber API > Inicia API de servicio para obtener la cuenta por el numero: %s",
                accountNumber);

        Account account = accountService.getAccountByNumber(accountNumber);

        LOG.info("@getAccountByNumber API > Finaliza API de servicio para obtener la cuenta por su numero");

        return Response.ok().entity(account).build();
    }

    @GET
    @Path("/{accountNumber}/balance")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El saldo de la cuenta se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = BigDecimal.class, examples = "1850000.00"))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los parámetros suministrados",
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
                    responseCode = "404",
                    description = "La cuenta con el número ingresado no existe o no está activa",
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
            summary = "Obtener saldo de una cuenta",
            description = "Retorna el saldo actual de una cuenta activa"
    )
    public Response getAccountBalance(
            @Parameter(
                    description = "Número de la cuenta a consultar el saldo",
                    example = "456789012",
                    required = true
            )
            @NotBlank(message = "El valor número de cuenta no puede ser nulo o vacío")
            @Pattern(regexp = "^[1-9]\\d{4,14}$", message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos")
            @PathParam("accountNumber") String accountNumber
    ) {

        LOG.infof("@getAccountBalance API > Inicia API del servicio para obtener el saldo de la cuenta con " +
                "el numero: %s", accountNumber);

        BigDecimal balance = accountService.getAccountBalance(accountNumber);

        LOG.info("@getAccountBalance API > Finaliza API del servicio para obtener el saldo de la cuenta");

        return Response.ok().entity(balance).build();
    }

    @POST
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "La cuenta se creó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados o la cuenta ya existe",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El valor número de cuenta no puede ser nulo o vacío",
                                        "El tipo de cuenta debe ser 'AHORROS' o 'CORRIENTE'",
                                        "La cuenta con el número de cuenta ingresado ya esta registrada"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "El cliente no existe o no está activo",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Not Found"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "El cliente con el numero de documento ingresado no existe"
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
            summary = "Crear una nueva cuenta",
            description = "Registra una cuenta bancaria asociada a un cliente existente"
    )
    public Response createAccount(
            @RequestBody(
                    description = "Información de la cuenta a registrar",
                    content = @Content(schema = @Schema(implementation = AccountDTO.class), example = """
                            {
                              "accountNumber": "456789012",
                              "accountType": "AHORROS",
                              "balance": 1850000,
                              "currency": "COP",
                              "customerDocumentNumber": "1023456790"
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.Post.class)
            @Valid AccountDTO account
    ) {

        LOG.infof("createAccount API > Inicia API del servicio para registrar la cuenta con numero: %s",
                account.getAccountNumber());

        accountService.registerAccount(account);

        LOG.info("createAccount API > Finaliza API del servicio para registrar una cuenta nueva");

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "La cuenta se actualizó correctamente",
                    content = @Content(schema = @Schema(implementation = Account.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El valor número de cuenta no puede ser nulo o vacío",
                                        "El tipo de cuenta debe ser 'AHORROS' o 'CORRIENTE'",
                                        "Para esta actualización No debe enviar el campo saldo",
                                        "La cuenta se encuentra en estado CERRADA y no puede ser modificada"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La cuenta o el cliente no existen o están inactivos",
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
            summary = "Actualizar una cuenta",
            description = "Actualiza los datos de una cuenta existente (no modifica el saldo; use el endpoint /balance)"
    )
    public Response updateAccount(
            @RequestBody(
                    description = "Información de la cuenta a actualizar",
                    content = @Content(schema = @Schema(implementation = AccountDTO.class), example = """
                            {
                              "accountNumber": "456789012",
                              "accountType": "AHORROS",
                              "currency": "COP",
                              "status": "ACTIVA",
                              "customerDocumentNumber": "1023456790"
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.Put.class)
            @Valid AccountDTO account
    ) {

        LOG.infof("@updateAccount API > Inicia API del servicio para actualizar los datos de la cuenta con " +
                "numero: %s", account.getAccountNumber());

        Account updatedAccount = accountService.updateAccount(account);

        LOG.info("@updateAccount API > Finaliza API del servicio para actualizar los datos de la cuenta");

        return Response.ok().entity(updatedAccount).build();
    }

    @PUT
    @Path("/balance")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "El saldo de la cuenta se actualizó correctamente. Endpoint diseñado para uso " +
                            "interno entre microservicios. (No visible en doc swagger)"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El valor número de cuenta no puede ser nulo o vacío",
                                        "El saldo de la cuenta no puede ser negativo"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "La cuenta con el número ingresado no existe o no está activa",
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
            summary = "Actualizar saldo de una cuenta",
            description = "Actualiza únicamente el saldo de una cuenta existente."
    )
    public Response updateAccountBalance(
            @RequestBody(
                    description = "Información de la cuenta a actualizar",
                    content = @Content(schema = @Schema(implementation = AccountDTO.class), example = """
                            {
                              "accountNumber": "456789012",
                              "balance": 1850000
                            }""")
            )
            @ConvertGroup(to = ValidationGroups.PutBalance.class)
            @Valid AccountDTO account
    ) {

        LOG.infof("@updateAccountBalance API > Inicia API del servicio para actualizar el saldo de la cuenta " +
                "con numero: %s", account.getAccountNumber());

        accountService.updateAccountBalance(account);

        LOG.info("@updateAccountBalance API > Finaliza API del servicio para actualizar el saldo de la cuenta");

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/{accountNumber}")
    @Operation(
            summary = "Cerrar una cuenta (borrado lógico)",
            description = "Cambia el estado de una cuenta a CERRADA sin eliminar el registro físicamente de la base de datos"
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "La cuenta se cerró correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los parámetros suministrados",
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
                    responseCode = "404",
                    description = "La cuenta con el número ingresado no existe o no está activa",
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
    public Response deleteAccount(
            @Parameter(
                    description = "Número de cuenta a eliminar",
                    example = "456789012",
                    required = true
            )
            @NotBlank(message = "El valor número de cuenta no puede ser nulo o vacío")
            @Pattern(regexp = "^[1-9]\\d{4,14}$", message = "El valor número de cuenta debe contener solo números entre 5 y 15 dígitos")
            @PathParam("accountNumber") String accountNumber
    ) {

        LOG.infof("@deleteAccount API > Inicia APi del servicio para eliminar los datos de ls cuenta con " +
                "numero: %s", accountNumber);

        accountService.deleteAccountByNumber(accountNumber);

        LOG.info("@deleteAccount API > Finaliza API del servicio para eliminar los datos de la cuenta");

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
