package org.uni.santo.tomas.helper.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.util.List;

@Getter
public class ASException extends RuntimeException {

    private final Response.Status status;
    private List<String> errors;

    public ASException(int statusCode, String message) {
        super(message);
        this.status = Response.Status.fromStatusCode(statusCode);
    }

    public ASException(int statusCode, String message, List<String> errors) {
        super(message);
        this.errors = errors;
        this.status = Response.Status.fromStatusCode(statusCode);
    }
}
