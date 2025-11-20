package com.flowdesk.flowdesk_backend.exception;

/**
 * Exception thrown when authentication fails
 * HTTP Status: 401 UNAUTHORIZED
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
