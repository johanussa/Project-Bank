package org.uni.santo.tomas.helper.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.uni.santo.tomas.helper.exception.ProblemException;

import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityInterceptor implements ContainerRequestFilter {

    @ConfigProperty(name = "CONSTANT.ACCOUNT-SERVICE.AUTH.API-KEY")
    String authApiKey;

    private static final Set<String> EXCLUDED_PATHS = Set.of("/q/openapi", "/q/swagger", "/health", "/q/health");

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String path = requestContext.getUriInfo().getPath();

        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) return;

        String providedKey = requestContext.getHeaderString("x-api-key");

        if (providedKey == null || !providedKey.equals(authApiKey)) {

            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(ProblemException.builder()
                                    .title(Response.Status.UNAUTHORIZED.getReasonPhrase())
                                    .detail("La API Key es inválida o esta ausente")
                                    .build())
                            .build()
            );
        }
    }
}
