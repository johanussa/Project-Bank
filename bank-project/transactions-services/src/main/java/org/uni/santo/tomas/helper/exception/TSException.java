package org.uni.santo.tomas.helper.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.util.List;

@Getter
public class TSException extends RuntimeException {

    private final Response.Status status;
    private List<String> errors;

    public TSException(int statusCode, String message) {
        super(message);
        this.status = Response.Status.fromStatusCode(statusCode);
    }
}
