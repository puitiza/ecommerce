package com.ecommerce.shared.openapi;

import com.ecommerce.shared.interfaces.openapi.response.ApiCommonErrors;
import com.ecommerce.shared.interfaces.openapi.response.ApiResourceNotFound;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

public interface CrudOpenApi<T, ID> {
    @Operation(summary = "Create Resource", description = "Creates a new resource",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiValidationErrors
    @ApiResponse(responseCode = "201", description = "Resource created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class)))
    T create(T resource);

    @Operation(summary = "Retrieve All Resources", description = "Retrieves all resources",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiCommonErrors
    @ApiResponse(responseCode = "200", description = "Resources retrieved successfully",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Object.class))))
    List<T> getAll();

    @Operation(summary = "Retrieve Resource by ID", description = "Retrieves a resource by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResourceNotFound
    @ApiResponse(responseCode = "200", description = "Resource retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class)))
    T getById(ID id);

    @Operation(summary = "Update Resource", description = "Updates an existing resource",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiValidationErrors
    @ApiResponse(responseCode = "200", description = "Resource updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class)))
    T update(ID id, T resource);

    @Operation(summary = "Delete Resource", description = "Deletes a resource by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResourceNotFound
    @ApiResponse(responseCode = "204", description = "Resource deleted successfully")
    void delete(ID id);
}