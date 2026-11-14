package org.uni.santo.tomas.helper.exception;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.logging.Logger;

public class ResponseMapperException implements ResponseExceptionMapper<RuntimeException> {

    private static final Logger LOG = Logger.getLogger(ResponseMapperException.class);

    @Override
    public RuntimeException toThrowable(Response response) {

        String body = "-";
        int status = response.getStatus();

        try {
            response.bufferEntity();
            if (response.hasEntity()) body = response.readEntity(String.class);
        } catch (IllegalStateException e) {
            body = "@toThrowable > Error al leer el flujo de la entidad";
            LOG.error(body, e);
        } catch (ProcessingException e) {
            body = "@toThrowable > No se pudo almacenar el flujo";
            LOG.error(body, e);
        }

        return new RuntimeException("@toThrowable-ResponseMapperException > Error presentado en servicio " +
                "rest client:status: " + status + " :body: " + body);
    }
}
