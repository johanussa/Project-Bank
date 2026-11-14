package org.uni.santo.tomas.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.repository.CustomerRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class CustomerApiTest {

    @ConfigProperty(name = "CONSTANT.ACCOUNT-SERVICE.AUTH.API-KEY")
    String API_KEY;

    @InjectMock
    CustomerRepository customerRepository;

    Customer customerOK = Customer.builder()
            .documentNumber("1023456789")
            .documentType("CC")
            .names("Juan Carlos")
            .lastNames("Rodriguez Torres")
            .email("juan.rodriguez@correo.com")
            .phone("3102345678")
            .address("Calle 72 # 12-03")
            .dateRegistration(LocalDateTime.of(2026, 11, 13, 10, 0))
            .status("ACTIVO")
            .build();

    @Test
    void when_sendRequestToGetCustomer_Expect_OK() {

        String documentNumber = "1023456789";
        Mockito.when(customerRepository.findCustomerByDocument(documentNumber)).thenReturn(Optional.of(customerOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("documentNumber", documentNumber)
                .get("/api/v1/accounts-service/customers/{documentNumber}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("documentNumber", equalTo("1023456789"))
                .body("documentType", equalTo("CC"))
                .body("names", equalTo("Juan Carlos"))
                .body("lastNames", equalTo("Rodriguez Torres"))
                .body("email", equalTo("juan.rodriguez@correo.com"))
                .body("phone", equalTo("3102345678"))
                .body("status", equalTo("ACTIVO"));
    }

    @Test
    void when_sendRequestToGetCustomer_Expect_NotFound() {

        String documentNumber = "9999999999";
        Mockito.when(customerRepository.findCustomerByDocument(documentNumber)).thenReturn(Optional.empty());

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("documentNumber", documentNumber)
                .get("/api/v1/accounts-service/customers/{documentNumber}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("title", equalTo("Not Found"))
                .body("detail", equalTo("El cliente con el numero de documento ingresado no existe"));
    }

    @Test
    void when_CreateCustomerWithInvalidEmail_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "documentNumber": "1023456789",
                                "documentType": "CC",
                                "names": "Juan Carlos",
                                "lastNames": "Rodriguez Torres",
                                "email": "correo-invalido",
                                "phone": "3102345678",
                                "address": "Calle 72 # 12-03"
                            }
                        """)
                .when()
                .post("/api/v1/accounts-service/customers")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El correo ingresado tiene un formato inválido"));
    }

    @Test
    void when_CreateCustomerWithoutNames_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "documentNumber": "1023456789",
                                "documentType": "CC",
                                "lastNames": "Rodriguez Torres",
                                "email": "juan.rodriguez@correo.com",
                                "phone": "3102345678",
                                "address": "Calle 72 # 12-03"
                            }
                        """)
                .when()
                .post("/api/v1/accounts-service/customers")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("Los nombres no pueden ser nulos o vacíos"));
    }

    @Test
    void when_DeleteCustomer_Expect_NoContent() {

        String documentNumber = "1023456789";
        Mockito.when(customerRepository.findCustomerByDocument(documentNumber)).thenReturn(Optional.of(customerOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("documentNumber", documentNumber)
                .delete("/api/v1/accounts-service/customers/{documentNumber}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(customerRepository, Mockito.times(1))
                .findCustomerByDocument(documentNumber);
    }
}
