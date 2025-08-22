package com.ecommerce.productservice.infrastructure.adapter.persistence.mapper;

import com.ecommerce.productservice.application.dto.ProductRequest;
import com.ecommerce.productservice.application.dto.ProductResponse;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
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

    /**
     * Converts a {@link Product} domain model to a {@link ProductEntity}.
     *
     * @param product the product domain model
     * @return the JPA entity
     */
    public ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.id());
        return this.setEntityFields(entity, product);
    }

    /**
     * Converts a {@link ProductEntity} to a {@link Product} domain model.
     *
     * @param entity the JPA entity
     * @return the product domain model
     */
    public Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getInventory(),
                entity.getImage(),
                entity.getCategories(),
                entity.getAdditionalData(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Updates the fields of a {@link ProductEntity} from a {@link Product} domain model.
     *
     * @param entity the JPA entity to update
     * @param domain the product domain model
     * @return the updated JPA entity
     */
    public ProductEntity setEntityFields(ProductEntity entity, Product domain) {
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setPrice(domain.price());
        entity.setInventory(domain.inventory());
        entity.setImage(domain.image());
        entity.setCategories(domain.categories());
        entity.setAdditionalData(domain.additionalData());
        return entity;
    }
}