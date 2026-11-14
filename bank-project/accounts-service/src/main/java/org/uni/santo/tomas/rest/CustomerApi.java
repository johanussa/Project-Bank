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
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.model.customer.CustomerDTO;
import org.uni.santo.tomas.service.CustomerService;

import java.util.List;

@Tag(name = "Clientes-API")
@Path("/api/v1/accounts-service/customers")
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
public class CustomerApi {

    private static final Logger LOG = Logger.getLogger(CustomerApi.class.getName());

    @Inject
    CustomerService customerService;

    @GET
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El listado de clientes se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Customer.class, type = SchemaType.ARRAY))
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
            summary = "Obtener listado de clientes",
            description = "Retorna un listado de los clientes registrados en el sistema"
    )
    public Response getAllCustomers() {

        LOG.info("@getAllCustomers API > Inicia API de servicio para obtener listado de clientes registrados");

        List<Customer> customers = customerService.getListCustomers();

        LOG.info("@getAllCustomers API > Finaliza API de servicio para obtener lista de clientes");

        return Response.ok().entity(customers).build();
    }

    @GET
    @Path("/{documentNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El cliente se obtuvo correctamente",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en el parámetro suministrado",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "Debe ingresar el identificador del cliente",
                                        "El número de documento debe contener solo números entre 5 y 15 dígitos"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "El cliente con el documento ingresado no existe o está inactivo",
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
            summary = "Obtener un cliente por número de documento",
            description = "Retorna los datos de un cliente activo según su número de documento"
    )
    public Response getCustomerByDocument(
            @Parameter(
                    description = "Número de documento del cliente a buscar",
                    example = "1023456789",
                    required = true
            )
            @NotBlank(message = "Debe ingresar el identificador del cliente")
            @Pattern(regexp = "^[0-9]{5,15}$", message = "El número de documento debe contener solo números entre 5 y 15 dígitos")
            @PathParam("documentNumber") String documentNumber
    ) {

        LOG.infof("@getCustomerDocument API > Inicia API de servicio para obtener cliente por el numero de " +
                "documento: %s", documentNumber);

        Customer customer = customerService.getCustomerByDocumentNumber(documentNumber);

        LOG.info("@getCustomerDocument API > Finaliza API de servicio para obtener cliente por documento");

        return Response.ok().entity(customer).build();
    }

    @POST
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "El cliente se creó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados o el cliente ya existe",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El número de documento no puede ser nulo o vacío",
                                        "El correo ingresado tiene un formato inválido",
                                        "El cliente con el número de documento ingresado ya esta registrado"
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
            summary = "Crear un nuevo cliente",
            description = "Registra un cliente en el sistema con estado ACTIVO y fecha de registro automática"
    )
    public Response createCustomer(
            @ConvertGroup(to = ValidationGroups.Post.class)
            @RequestBody(description = "Información del cliente a registrar")
            @Valid CustomerDTO customer
    ) {

        LOG.infof("createCustomer API > Inicia API del servicio para registrar el cliente con numero de " +
                "documento: %s", customer.getDocumentNumber());

        customerService.registerACustomer(customer);

        LOG.info("createCustomer API > Finaliza API del servicio para registrar un cliente");

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{documentNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "El cliente se actualizó correctamente",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en los datos suministrados",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "El correo ingresado tiene un formato inválido",
                                        "Los nombres no pueden ser nulos o vacíos",
                                        "El tipo de documento no puede ser nulo o vacío",
                                        "El número de documento debe contener solo números entre 5 y 15 dígitos"
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
                    description = "El cliente con el documento ingresado no existe o está inactivo",
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
            summary = "Actualizar un cliente existente",
            description = "Actualiza los datos editables de un cliente activo. El número de documento no se puede modificar."
    )
    public Response updateCustomer(
            @Parameter(
                    description = "Número de documento del cliente a actualizar",
                    example = "1023456789",
                    required = true
            )
            @NotBlank(message = "Debe ingresar el identificador del cliente")
            @Pattern(regexp = "^[0-9]{5,15}$", message = "El número de documento debe contener solo números entre 5 y 15 dígitos")
            @PathParam("documentNumber") String documentNumber,

            @RequestBody(
                    description = "Información del cliente a actualizar",
                    content = @Content(schema = @Schema(implementation = CustomerDTO.class, examples = """
                            {
                                "documentType": "CC",
                                "names": "Juan Carlos",
                                "lastNames": "Rodriguez Torres",
                                "email": "juan.rodriguez@correo.com",
                                "phone": "3102345678",
                                "address": "Calle 72 # 12-03"
                            }"""))
            )
            @ConvertGroup(to = ValidationGroups.Put.class)
            @Valid CustomerDTO customer
    ) {

        LOG.infof("@updateCustomer API > Inicia API del servicio para actualizar los datos del cliente con " +
                "numero de documento: %s", documentNumber);

        Customer updatedClient = customerService.updateACustomer(customer, documentNumber);

        LOG.info("@updateCustomer API > Finaliza API del servicio para actualizar los datos del cliente");

        return Response.ok().entity(updatedClient).build();
    }

    @DELETE
    @Path("/{documentNumber}")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "204",
                    description = "El cliente se inactivó correctamente"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Error en el parámetro suministrado",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Bad Request"),
                            @SchemaProperty(name = "errors", examples = """
                                    [
                                        "Debe ingresar el identificador del cliente",
                                        "El número de documento debe contener solo números entre 5 y 15 dígitos"
                                    ]""")
                    }))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "El cliente con el documento ingresado no existe o ya está inactivo",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Not Found"),
                            @SchemaProperty(
                                    name = "detail",
                                    examples = "El cliente con el numero de documento ingresado no existe"
                            )
                    }))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "No se permite desactivar el cliente",
                    content = @Content(schema = @Schema(implementation = ProblemException.class, properties = {
                            @SchemaProperty(name = "title", examples = "Conflict"),
                            @SchemaProperty(name = "detail", examples = "No se permite inactivar un cliente que tiene cuentas activas")
                    }))
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = HandlerException.ResponseError.class))
            )
    })
    @Operation(
            summary = "Inactivar un cliente (borrado lógico)",
            description = "Cambia el estado del cliente a INACTIVO sin eliminar el registro físicamente de la base de datos"
    )
    public Response deleteCustomer(
            @Parameter(
                    description = "Número de documento del cliente a eliminar",
                    example = "1023456789",
                    required = true
            )
            @NotBlank(message = "Debe ingresar el identificador del cliente")
            @Pattern(regexp = "^[0-9]{5,15}$", message = "El número de documento debe contener solo números entre 5 y 15 dígitos")
            @PathParam("documentNumber") String documentNumber
    ) {

        LOG.infof("@deleteCustomer API > Inicia APi del servicio para eliminar los datos del cliente " +
                "identificado con numero de documento: %s", documentNumber);

        customerService.deleteCustomerByDocumentNumber(documentNumber);

        LOG.info("@deleteCustomer API > Finaliza API del servicio para eliminar los datos del cliente");

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
