package org.uni.santo.tomas.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.uni.santo.tomas.model.account.Account;
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.repository.AccountRepository;
import org.uni.santo.tomas.repository.CustomerRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class AccountApiTest {

    @ConfigProperty(name = "CONSTANT.ACCOUNT-SERVICE.AUTH.API-KEY")
    String API_KEY;

    @InjectMock
    AccountRepository accountRepository;

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

    Account accountOK = Account.builder()
            .accountNumber("456789012")
            .accountType("AHORROS")
            .balance(new BigDecimal("1850000"))
            .currency("COP")
            .status("ACTIVA")
            .openingDate(LocalDateTime.of(2026, 11, 13, 10, 0))
            .customer(customerOK)
            .build();

    @Test
    void when_sendRequestToGetAccount_Expect_OK() {

        String accountNumber = "456789012";
        Mockito.when(accountRepository.findAccountByNumber(accountNumber)).thenReturn(Optional.of(accountOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("accountNumber", accountNumber)
                .get("/api/v1/accounts-service/accounts/{accountNumber}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("accountNumber", equalTo("456789012"))
                .body("accountType", equalTo("AHORROS"))
                .body("balance", equalTo(1850000))
                .body("currency", equalTo("COP"))
                .body("status", equalTo("ACTIVA"));
    }

    @Test
    void when_sendRequestToGetAccountBalance_Expect_OK() {

        String accountNumber = "456789012";
        Mockito.when(accountRepository.findAccountByNumber(accountNumber)).thenReturn(Optional.of(accountOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("accountNumber", accountNumber)
                .get("/api/v1/accounts-service/accounts/{accountNumber}/balance")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(equalTo("1850000"));
    }

    @Test
    void when_CreateAccountWithInvalidType_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "accountNumber": "456789012",
                                "accountType": "INVALIDA",
                                "balance": 1850000,
                                "currency": "COP",
                                "customerDocumentNumber": "1023456789"
                            }
                        """)
                .when()
                .post("/api/v1/accounts-service/accounts")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("El tipo de cuenta debe ser 'AHORROS' o 'CORRIENTE'"));
    }

    @Test
    void when_CreateAccountWithInvalidCurrency_Expect_BadRequest() {

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .body("""
                            {
                                "accountNumber": "456789012",
                                "accountType": "AHORROS",
                                "balance": 1850000,
                                "currency": "GBP",
                                "customerDocumentNumber": "1023456789"
                            }
                        """)
                .when()
                .post("/api/v1/accounts-service/accounts")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("Error en los recursos suministrados"))
                .body("errors", hasSize(1))
                .body("errors", hasItem("La moneda debe ser 'COP', 'USD' o 'EUR'"));
    }

    @Test
    void when_DeleteAccount_Expect_NoContent() {

        String accountNumber = "456789012";
        Mockito.when(accountRepository.findAccountByNumber(accountNumber)).thenReturn(Optional.of(accountOK));

        given()
                .contentType(ContentType.JSON)
                .header("x-api-key", API_KEY)
                .when()
                .pathParam("accountNumber", accountNumber)
                .delete("/api/v1/accounts-service/accounts/{accountNumber}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(accountRepository, Mockito.times(1)).findAccountByNumber(accountNumber);
    }
}