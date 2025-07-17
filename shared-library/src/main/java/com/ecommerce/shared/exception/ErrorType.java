package com.ecommerce.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    UNPROCESSABLE(HttpStatus.UNPROCESSABLE_ENTITY, "Validation error. Check 'errors' field for details.", ErrorCodes.VALIDATION_ERROR),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found", ErrorCodes.NOT_FOUND),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication failed", ErrorCodes.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied", ErrorCodes.FORBIDDEN),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", ErrorCodes.RATE_LIMIT_EXCEEDED),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Error unknow",ErrorCodes.INTERNAL_ERROR);

    private final HttpStatus status;
    private final String defaultMessage;
    private final String defaultErrorCode;

    ErrorType(HttpStatus status, String defaultMessage, String defaultErrorCode) {
        this.status = status;
        this.defaultMessage = defaultMessage;
        this.defaultErrorCode = defaultErrorCode;
    }

    public ErrorResponse create(String message, String errorCode) {
        return new ErrorResponse(this, message, errorCode);
    }
}
