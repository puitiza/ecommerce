package com.ecommerce.shared.exception;

public class ResourceNotFoundException extends HandledException {
    private String code;

    public ResourceNotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    // Constructor to match API Gateway's RateLimitExceededException which only takes a message
    public ResourceNotFoundException(String message) {
        super(message);
        this.code = "EC-404"; // Default code if not provided
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
