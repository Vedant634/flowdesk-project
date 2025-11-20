package com.flowdesk.flowdesk_backend.exception;

/**
 * Exception thrown when there's a conflict with existing resource
 * HTTP Status: 409 CONFLICT
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
