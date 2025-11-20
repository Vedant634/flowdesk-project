package com.flowdesk.flowdesk_backend.exception;

/**
 * Exception thrown when user doesn't have permission to access resource
 * HTTP Status: 403 FORBIDDEN
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
