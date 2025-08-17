package com.ecommerce.shared.interfaces.openapi.response;

import com.ecommerce.shared.domain.model.ErrorResponse;
import com.ecommerce.shared.interfaces.openapi.ResponseApiTemplate;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API response for validation errors (e.g., malformed request body).
 * Applicable to POST, PUT, and PATCH methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiResponse(responseCode = "422", description = "Invalid request data", content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE)))
public @interface ApiValidationErrors {
}
