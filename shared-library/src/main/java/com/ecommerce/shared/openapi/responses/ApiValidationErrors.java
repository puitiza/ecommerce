package com.ecommerce.shared.openapi.responses;

import com.ecommerce.shared.exception.ErrorResponse;
import com.ecommerce.shared.openapi.ResponseApiTemplate;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiCommonErrors
@ApiResponse(responseCode = "422", description = "Invalid request data", content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE)))
public @interface ApiValidationErrors {
}
