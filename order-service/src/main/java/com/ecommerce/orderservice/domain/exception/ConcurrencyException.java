package com.ecommerce.orderservice.domain.exception;

/**
 * Custom exception to represent an error during a concurrent operation.
 * It's a type of infrastructure exception that signals a problem with the
 * execution of parallel tasks, such as an unexpected interruption.
 */
public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}