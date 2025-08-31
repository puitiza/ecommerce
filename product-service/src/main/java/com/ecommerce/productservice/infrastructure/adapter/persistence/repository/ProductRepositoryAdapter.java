package com.ecommerce.productservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.ProductRepositoryPort;
import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
import com.ecommerce.productservice.infrastructure.adapter.persistence.mapper.ProductEntityMapper;
import com.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter implementing {@link ProductRepositoryPort} for database operations using Spring Data JPA.
 * Maps between domain models and JPA entities.
 * <p>
 * Caching is enabled for {@code findAllByIds} but requires an external cache provider (e.g., Redis).
 * Ensure {@code spring.cache.type=redis} is set in application properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper mapper;

    /**
     * Saves a product to the database and immediately flushes the changes.
     * <p>
     * The {@code saveAndFlush} method ensures that the entity is persisted and
     * the database transaction is synchronized, allowing immediate retrieval
     * of auto-generated fields (e.g., {@code id}, {@code createdAt}, {@code updatedAt}).
     * This is critical for ensuring consistency in event-driven workflows.
     * </p>
     *
     * @param product The product domain model to save.
     * @return The saved product with updated database-generated fields.
     */
    @Override
    public Product save(Product product) {
        var productEntity = mapper.toEntity(product);
        var savedEntity = jpaRepository.saveAndFlush(productEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Product findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }

    @Override
    public Page<Product> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        var entities = jpaRepository.findAll(pageable);
        return new PageImpl<>(entities.getContent().stream().map(mapper::toDomain).toList(),
                pageable, entities.getTotalElements());
    }

    @Override
    public void delete(Long id) {
        var productFound = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
        jpaRepository.delete(productFound);
    }

    @Override
    public Product update(Long id, Product product) {
        ProductEntity entity = mapper.toEntity(product);
        entity.setId(id);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Optional<Product> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    @Cacheable(value = "products", unless = "#result.isEmpty()")
    public List<Product> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Retrieves products by color with pagination, using a native SQL query to
     * extract the color from the JSONB {@code additionalData} column.
     *
     * @param color the color to filter by
     * @param page  the page number (0-based)
     * @param size  the number of items per page
     * @return a paginated list of products matching the color
     */
    @Override
    public Page<Product> findByColor(String color, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<ProductEntity> entities = jpaRepository.findByColor(color, pageable);
        return new PageImpl<>(entities.getContent().stream().map(mapper::toDomain).toList(),
                pageable, entities.getTotalElements());
    }

    @Override
    @CacheEvict(value = "products", allEntries = true) // Clear cache for all products
    public List<Product> updateStockInBatch(Map<Long, Integer> productIdToStock) {
        List<ProductEntity> entities = jpaRepository.findAllByIdIn(productIdToStock.keySet().stream().toList()).stream()
                .map(entity -> {
                    Product product = mapper.toDomain(entity);
                    Integer newInventory = productIdToStock.get(product.id());
                    if (newInventory == null) {
                        return entity; // No update needed
                    }
                    Product updatedProduct = product.updateInventory(newInventory);
                    ProductEntity updatedEntity = mapper.toEntity(updatedProduct);
                    updatedEntity.setId(product.id());
                    return updatedEntity;
                })
                .toList();
        List<ProductEntity> savedEntities = jpaRepository.saveAllAndFlush(entities);
        log.info("Batch updated stock for {} products", productIdToStock.size());
        return savedEntities.stream().map(mapper::toDomain).toList();
    }
}
