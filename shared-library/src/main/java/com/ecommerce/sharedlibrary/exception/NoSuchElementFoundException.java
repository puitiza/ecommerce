package com.ecommerce.sharedlibrary.exception;

public class NoSuchElementFoundException extends HandledException {
    private String code;

    public NoSuchElementFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    // Constructor to match API Gateway's RateLimitExceededException which only takes a message
    public NoSuchElementFoundException(String message) {
        super(message);
        this.code = "EC-404"; // Default code if not provided
    }

    @Override
    public String getErrorCode() {
        return code;
    }
}
