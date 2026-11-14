package org.uni.santo.tomas.helper.exception;

import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

@Provider
public class HandlerException implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(HandlerException.class);

    @Inject
    jakarta.inject.Provider<HttpServerRequest> httpServerRequestProvider;

    @Override
    public Response toResponse(Exception exception) {
        return mapExceptionToResponse(exception);
    }

    private Response mapExceptionToResponse(Exception exception) {

        if (exception instanceof TSException ex) {

            ProblemException problemException = ProblemException.builder()
                    .host(httpServerRequestProvider.get().getHeader("Host"))
                    .title(ex.getStatus().getReasonPhrase())
                    .detail(ex.getMessage())
                    .uri(httpServerRequestProvider.get().absoluteURI())
                    .errors(ex.getErrors())
                    .build();

            if (problemException.getErrors() != null && !problemException.getErrors().isEmpty()) {
                problemException.setDetail("Error en la solicitud: " + httpServerRequestProvider.get().absoluteURI());
            }
            return Response.status(ex.getStatus()).entity(problemException).build();
        }
        if (exception instanceof WebApplicationException webApplicationException) {

            Response originalErrorResponse = webApplicationException.getResponse();

            return Response.fromResponse(originalErrorResponse)
                    .entity(exception.getMessage())
                    .build();
        }

        UUID idError = UUID.randomUUID();

        LOG.errorf(exception, "@mapExceptionToResponse - Failed to process, idError: %s - request to: %s",
                idError.toString(), httpServerRequestProvider.get().absoluteURI());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        exception.printStackTrace(pw);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(
                        new ResponseError(
                                String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()),
                                idError.toString(),
                                "Internal Server Error"
                        )
                )
                .build();
    }

    public record ResponseError(
            @Schema(examples = "500") String code,
            @Schema(examples = "ef156aec-3d78-4f78-9b69-1dd8154f993d") String idError,
            @Schema(examples = "Internal Server Error") String msg
    ) {
    }
}
