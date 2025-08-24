package com.ecommerce.productservice.infrastructure.adapter.persistence.mapper;

import com.ecommerce.productservice.application.dto.ProductRequest;
import com.ecommerce.productservice.application.dto.ProductResponse;
import com.ecommerce.productservice.domain.model.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between domain models, DTOs, and JPA entities.
 * Used by the infrastructure layer to bridge the domain and persistence layers.
 */
@Component
public class ProductMapper {

    /**
     * Converts a {@link ProductRequest} to a {@link Product} domain model.
     *
     * @param request the DTO containing product data
     * @param id      the product ID (null for new products)
     * @return the product domain model
     */
    public Product toProduct(ProductRequest request, Long id) {
        return new Product(
                id,
                request.name(),
                request.description(),
                request.price(),
                request.inventory(),
                request.image(),
                request.categories(),
                request.additionalData(),
                null,
                null,
                null
        );
    }

    /**
     * Converts a {@link Product} domain model to a {@link ProductResponse} DTO.
     *
     * @param product the product domain model
     * @return the response DTO
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id() != null ? product.id().toString() : null,
                product.name(),
                product.description(),
                product.price(),
                product.inventory(),
                product.image(),
                product.categories(),
                product.additionalData(),
                product.createdAt(),
                product.updatedAt()
        );
    }

}