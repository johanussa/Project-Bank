package org.uni.santo.tomas.restClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.uni.santo.tomas.helper.exception.ResponseMapperException;
import org.uni.santo.tomas.model.Account;

@Path("/api/v1/accounts-service")
@RegisterRestClient(configKey = "accounts-service")
@RegisterProvider(value = ResponseMapperException.class)
public interface AccountProxy {

    @GET
    @Path("/accounts/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Account getAccountByNumber(@PathParam("accountNumber") String accountNumber);

    @PUT
    @Path("/accounts/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void updateAccountBalance(Account account);
}
