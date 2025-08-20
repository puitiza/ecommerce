package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString(exclude = "id")
public class ProductDto {

    @Schema(name = "id", description = "Unique identifier for the product", example = "123")
    private Long id;

    @NotBlank(message = "name field not should be null or empty")
    @Schema(name = "name", description = "Name of product", example = "xiaomi", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "description", description = "Description of the product", example = "A comfortable and stylish T-Shirt.")
    private String description;

    @NotNull(message = "price field not should be null or empty")
    @Schema(name = "price", description = "Price of the product", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(name = "inventory", description = "Current inventory level of the product", example = "100")
    private Integer inventory;

    @Schema(name = "image", description = "URL of the product image", example = "https://example.com/product-image.png")
    private String image;

    @Schema(name = "categories", description = "List of categories the product belongs to", example = "[\"Clothing\", \"T-Shirts\"]")
    private List<String> categories;

}
