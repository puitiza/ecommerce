package com.ecommerce.shared.exception;

public class ErrorCodes {
    // General errors
    public static final String VALIDATION_ERROR = "GEN-001";
    public static final String NOT_FOUND = "GEN-002";
    public static final String UNAUTHORIZED = "GEN-003";
    public static final String FORBIDDEN = "GEN-004";
    public static final String RATE_LIMIT_EXCEEDED = "GEN-005";
    public static final String INTERNAL_ERROR = "GEN-006";

    // Order service errors
    public static final String ORDER_VALIDATION = "ORD-003";
    public static final String ORDER_CANCELLATION = "ORD-004";
    public static final String ORDER_NOT_FOUND = "ORD-002";

    // Product service errors
    public static final String PRODUCT_NOT_FOUND = "PROD-002";
    public static final String PRODUCT_INVALID_INVENTORY = "PROD-003";
    public static final String PRODUCT_UPDATE_FAILED = "PROD-004";

    public static final String USER_INVENTORY = "USER-002";

    // API Gateway errors
    public static final String GATEWAY_UNAUTHORIZED = "EC-001";
    public static final String GATEWAY_FORBIDDEN = "EC-003";
    public static final String GATEWAY_RATE_LIMIT = "EC-004";
    public static final String GATEWAY_UNEXPECTED = "EC-005";
}
