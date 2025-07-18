package com.ecommerce.shared.exception;

import lombok.Getter;

@Getter
public enum ExceptionError {
    // General errors
    VALIDATION_ERROR("error.validation"),
    NOT_FOUND("error.not_found"),
    UNAUTHORIZED("error.unauthorized"),
    FORBIDDEN("error.forbidden"),
    RATE_LIMIT_EXCEEDED("error.rate_limit_exceeded"),
    INTERNAL_SERVER_ERROR("error.internal_server"),

    // Order service errors
    ORDER_VALIDATION("error.order.validation"),
    ORDER_CANCELLATION("error.order.cancellation"),

    // Product service errors
    PRODUCT_INVALID_INVENTORY("error.product.invalid_inventory"),
    PRODUCT_UPDATE_FAILED("error.product.update_failed"),

    // User service errors
    USER_USERNAME_FOUND("error.user.username_found"),
    USER_EMAIL_FOUND("error.user.email_found"),
    USER_ROLE_NOT_FOUND("error.user.role_not_found"),

    // API Gateway errors
    GATEWAY_UNAUTHORIZED("error.gateway.unauthorized"),
    GATEWAY_FORBIDDEN("error.gateway.forbidden"),
    GATEWAY_RATE_LIMIT("error.gateway.rate_limit"),
    GATEWAY_UNEXPECTED("error.gateway.unexpected");

    private final String key;

    ExceptionError(String key) {
        this.key = key;
    }
}