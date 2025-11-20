package com.flowdesk.flowdesk_backend.exception;

/**
 * Exception thrown when the request is invalid or malformed
 * HTTP Status: 400 BAD REQUEST
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
