package com.ecommerce.shared.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum defining error codes and HTTP statuses for common and service-specific exceptions.
 * Used across microservices for consistent error handling.
 */
@Getter
public enum ExceptionError {
    // General errors
    VALIDATION_ERROR("error.validation", HttpStatus.BAD_REQUEST),
    NOT_FOUND("error.not_found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("error.internal_server", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("error.service_unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // Order service errors
    ORDER_VALIDATION("error.order.validation", HttpStatus.BAD_REQUEST),
    ORDER_DUPLICATE_PRODUCT("error.order.duplicate_product", HttpStatus.UNPROCESSABLE_ENTITY),
    ORDER_INSUFFICIENT_INVENTORY("error.order.insufficient_inventory", HttpStatus.UNPROCESSABLE_ENTITY),
    ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED("error.order.product_availability_check_failed", HttpStatus.UNPROCESSABLE_ENTITY),
    ORDER_UPDATE_FAILED("error.order.update_failed", HttpStatus.BAD_REQUEST),
    ORDER_CANCELLATION("error.order.cancellation", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_AUTHORIZATION_FAILED("error.order.payment_authorization_failed", HttpStatus.BAD_REQUEST),
    ORDER_REFUND_FAILED("error.order.refund_failed", HttpStatus.BAD_REQUEST),
    ORDER_PAYMENT_LOOKUP_FAILED("error.order.payment_lookup_failed", HttpStatus.BAD_REQUEST),

    // Product service errors
    PRODUCT_INVALID_INVENTORY("error.product.invalid_inventory", HttpStatus.BAD_REQUEST),
    PRODUCT_UPDATE_FAILED("error.product.update_failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // User service errors
    USER_USERNAME_FOUND("error.user.username_found", HttpStatus.BAD_REQUEST),
    USER_EMAIL_FOUND("error.user.email_found", HttpStatus.BAD_REQUEST),
    USER_ROLE_NOT_FOUND("error.user.role_not_found", HttpStatus.BAD_REQUEST),

    // API Gateway errors
    GATEWAY_UNAUTHORIZED("error.gateway.unauthorized", HttpStatus.UNAUTHORIZED),
    GATEWAY_FORBIDDEN("error.gateway.forbidden", HttpStatus.FORBIDDEN),
    GATEWAY_RATE_LIMIT("error.gateway.rate_limit", HttpStatus.TOO_MANY_REQUESTS),
    GATEWAY_UNEXPECTED("error.gateway.unexpected", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String key;
    private final HttpStatus httpStatus;

    ExceptionError(String key, HttpStatus httpStatus) {
        this.key = key;
        this.httpStatus = httpStatus;
    }
}