package org.uni.santo.tomas.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.uni.santo.tomas.model.Transaction;
import org.uni.santo.tomas.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class TransactionApiTest {

    @ConfigProperty(name = "CONSTANT.ACCOUNT-SERVICE.AUTH.API-KEY")
    String API_KEY;

    @InjectMock
    TransactionRepository transactionRepository;

    Transaction transactionOK = Transaction.builder()
            .referenceNumber("TX-20261110-001")
            .type("DEPOSITO")
            .amount(new BigDecimal(1870000))
            .destinationAccountId("456789012")
            .transactionDate(LocalDateTime.of(2026, 11, 13, 14, 48))
            .status("COMPLETADA")
            .description("DEPOSITO")
            .build();

    @Test
    void when_sendRequestToGetTransaction_Expect_OK() {

        String referenceNumber = "TX-20261110-001";
        Mockito.when(transactionRepository.findByIdOptional(referenceNumber)).thenReturn(Optional.of(transactionOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("referenceNumber", "TX-20261110-001")
                .get("/api/v1/transactions-service/{referenceNumber}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("referenceNumber", equalTo("TX-20261110-001"))
                .body("type", equalTo("DEPOSITO"))
                .body("amount", equalTo(1870000))
                .body("originAccountId", equalTo(null))
                .body("destinationAccountId", equalTo("456789012"))
                .body("transactionDate", equalTo("2026-11-13T14:48:00"))
                .body("status", equalTo("COMPLETADA"))
                .body("description", equalTo("DEPOSITO"));
    }

    @Test
    void when_sendRequestToGetTransaction_Expect_NotFound() {

        String referenceNumber = "TX-20261110-001";
        Mockito.when(transactionRepository.findByIdOptional(referenceNumber)).thenReturn(Optional.empty());

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("referenceNumber", "TX-20261110-001")
                .get("/api/v1/transactions-service/{referenceNumber}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("title", equalTo("Not Found"))
                .body("detail", equalTo("La transacción con referencia numero: TX-20261110-001 no existe"));
    }

    @Test
    void when_DepositWithNegativeAmount_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "destinationAccountId": "456789012",
                                "amount": -500
                            }
                        """)
                .when()
                .post("/api/v1/transactions-service/deposit")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El monto del depósito debe ser mayor a cero"));
    }

    @Test
    void when_DepositWithoutDestinationAccount_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "amount": 100000
                            }
                        """)
                .when()
                .post("/api/v1/transactions-service/deposit")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El número de cuenta destino no puede ser nulo o vacío"));
    }

    @Test
    void when_WithdrawWithoutOriginAccount_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "amount": 100000
                            }
                        """)
                .when()
                .post("/api/v1/transactions-service/withdraw")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El valor número de cuenta no puede ser nulo o vacío"));
    }

    @Test
    void when_TransferWithoutDestinationAccount_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "originAccountId": "456789012",
                                "amount": 100000
                            }
                        """)
                .when()
                .post("/api/v1/transactions-service/transfer")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El número de cuenta destino no puede ser nulo o vacío"));
    }

    @Test
    void when_RequestWithoutApiKey_Expect_Unauthorized() {

        given()
                .contentType(ContentType.JSON)
                .body("""
                            {
                                "destinationAccountId": "456789012",
                                "amount": 100000
                            }
                        """)
                .when()
                .post("/api/v1/transactions-service/deposit")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .body("title", equalTo("Unauthorized"))
                .body("detail", equalTo("La API Key es inválida o esta ausente"));
    }
}