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
        return this.setEntityFields(entity, product);
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