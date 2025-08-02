package com.ecommerce.shared.openapi;

public class ResponseApiTemplate {

    public static final String UNPROCESSABLE = """
            {
                "status": 422,
                "errorCode": "P01",
                "message": "Validation error. Check 'errors' field for details.",
                "details": "",
                "timestamp": "04-01-2024 04:55:12 PET",
                "stackTrace": [],
                "errors": [
                    {
                        "field": "orderId",
                        "message": "'orderId' field not should be null or empty"
                    }
                ]
            }
            """;
    public static final String UNAUTHORIZED = """
            {
                "status": 401,
                "errorCode": "P02",
                "message": "Unauthorized access. Invalid or missing JWT token.",
                "details": "Invalid token provided",
                "timestamp": "2025-08-01T10:07:34-05:00",
                "stackTrace": []
            }
            """;
    public static final String FORBIDDEN = """
            {
                "status": 403,
                "errorCode": "P03",
                "message": "Forbidden. Insufficient permissions.",
                "details": "User role is not authorized for this resource",
                "timestamp": "2025-08-01T10:07:34-05:00",
                "stackTrace": []
            }
            """;
    public static final String RATE_LIMIT = """
            {
                "status": 429,
                "errorCode": "P04",
                "message": "Rate limit exceeded. Try again later.",
                "details": "Too many requests in a given amount of time.",
                "timestamp": "04-01-2024 07:23:34 PET",
                "stackTrace": []
            }
            """;
    public static final String NOT_FOUND = """
            {
                "status": 404,
                "errorCode": "P05",
                "message": "Resource not found.",
                "details": "The requested resource does not exist.",
                "timestamp": "2025-08-01T10:07:34-05:00",
                "stackTrace": []
            }
            """;
}
