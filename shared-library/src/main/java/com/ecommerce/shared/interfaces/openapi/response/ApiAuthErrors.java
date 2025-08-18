package com.ecommerce.shared.interfaces.openapi.response;

import com.ecommerce.shared.domain.model.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OpenAPI annotation for authentication/authorization errors (HTTP 401, 403).
 * Applicable to all API endpoints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "unauthorized",
                                description = "Unauthorized access due to invalid token",
                                value = """
                                        {
                                          "status": 401,
                                          "errorCode": "EC-001",
                                          "message": "Unauthorized access. Invalid or missing JWT token.",
                                          "details": "Invalid token provided",
                                          "timestamp": "2025-08-18T02:14:54.361195666Z",
                                          "stackTrace": [],
                                          "errors": null
                                        }
                                        """))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "forbidden",
                                description = "Forbidden due to insufficient permissions",
                                value = """
                                        {
                                          "status": 403,
                                          "errorCode": "EC-002",
                                          "message": "Forbidden. Insufficient permissions.",
                                          "details": "User role is not authorized for this resource",
                                          "timestamp": "2025-08-18T02:14:54.361195666Z",
                                          "stackTrace": [],
                                          "errors": null
                                        }
                                        """)))
})
public @interface ApiAuthErrors {
}