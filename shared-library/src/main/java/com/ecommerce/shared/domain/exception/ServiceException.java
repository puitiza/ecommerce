package com.ecommerce.shared.domain.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class ServiceException extends RuntimeException {
    private final ExceptionError error;
    private final Map<String, Object> metadata = new HashMap<>();
    private final Object[] messageArgs;

    protected ServiceException(ExceptionError error, String message, Object... messageArgs) {
        super(message);
        this.error = error;
        this.messageArgs = messageArgs;
    }

    protected ServiceException(ExceptionError error, String message, Throwable cause, Object... messageArgs) {
        super(message, cause);
        this.error = error;
        this.messageArgs = messageArgs;
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}