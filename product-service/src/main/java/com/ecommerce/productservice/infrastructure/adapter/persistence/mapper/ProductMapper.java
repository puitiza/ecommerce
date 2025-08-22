package com.ecommerce.productservice.infrastructure.adapter.persistence.mapper;

import com.ecommerce.productservice.application.dto.ProductRequest;
import com.ecommerce.productservice.application.dto.ProductResponse;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

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
        return entity;
    }

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
}