package com.ecommerce.productservice.infrastructure.adapter.persistence.mapper;

import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

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
                entity.getUpdatedAt(),
                entity.getVersion()
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
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setPrice(product.price());
        entity.setInventory(product.inventory());
        entity.setImage(product.image());
        entity.setCategories(product.categories());
        entity.setAdditionalData(product.additionalData());
        entity.setCreatedAt(product.createdAt());
        entity.setUpdatedAt(product.updatedAt());
        entity.setVersion(product.version());
        return entity;
    }

}