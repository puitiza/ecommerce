package com.ecommerce.shared.exception;

public class ResourceNotFoundException extends HandledException {
    private final String code;

    public ResourceNotFoundException(String message, String code) {
        super(message);
        this.code = code != null ? code : "EC-404";
    }

    public ResourceNotFoundException(String message) {
        this(message, "EC-404");
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}