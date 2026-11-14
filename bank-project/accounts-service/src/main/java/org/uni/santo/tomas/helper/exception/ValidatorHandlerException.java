package org.uni.santo.tomas.helper.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ValidatorHandlerException implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        List<String> errors = exception.getConstraintViolations()
                .stream().map(ConstraintViolation::getMessage)
                .toList();

        ProblemException problemException = ProblemException.builder()
                .title("Solicitud inválida")
                .detail("Error en los recursos suministrados")
                .errors(errors)
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(problemException)
                .build();
    }
}
