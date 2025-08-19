package com.ecommerce.shared.interfaces.openapi;

import com.ecommerce.shared.interfaces.openapi.response.ApiAuthErrors;
import com.ecommerce.shared.interfaces.openapi.response.ApiCommonErrors;
import com.ecommerce.shared.interfaces.openapi.response.ApiResourceNotFound;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Generic OpenAPI interface for standard CRUD operations.
 * Defines endpoints for creating, retrieving, updating, and deleting resources.
 * Type parameters:
 * - T: The response DTO type (e.g., OrderResponse).
 * - V: The request DTO type (e.g., OrderRequest).
 * - ID: The identifier type for the resource (e.g., UUID, Long).
 * Intended to be implemented by service-specific controllers.
 */
@ApiCommonErrors
@ApiAuthErrors
@RequestMapping
public interface CrudOpenApi<T, V, ID> {

    @ApiValidationErrors
    @Operation(summary = "Create Resource",
            description = "Creates a new resource with the provided details.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "201", description = "Resource created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    T create(@Parameter(description = "Details of the resource to create", required = true)
             @RequestBody V request);

    @ApiResourceNotFound
    @Operation(summary = "Retrieve Resource by ID",
            description = "Retrieves the details of a resource by its ID.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Resource retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    T getById(@Parameter(description = "ID of the resource to retrieve", required = true)
              @PathVariable ID id);

    @ApiValidationErrors
    @ApiResourceNotFound
    @Operation(summary = "Update Resource",
            description = "Updates an existing resource by its ID.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Resource updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    T update(@Parameter(description = "ID of the resource to update", required = true)
             @PathVariable ID id,
             @Parameter(description = "Updated resource details", required = true)
             @RequestBody V request);

    @ApiResourceNotFound
    @Operation(summary = "Delete Resource",
            description = "Deletes a resource by its ID.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Resource deleted successfully")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@Parameter(description = "ID of the resource to delete", required = true)
                @PathVariable ID id);
}