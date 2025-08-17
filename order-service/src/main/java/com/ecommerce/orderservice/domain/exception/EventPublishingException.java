package com.ecommerce.orderservice.domain.exception;

/**
 * Custom runtime exception for infrastructure failures, such as event publishing.
 * <p>
 * This exception should not be handled by public-facing API endpoints. It signals
 * an internal system error, not a client-side one. It typically results in
 * a 500 Internal Server Error and is intended for internal logging and monitoring.
 */
public class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
