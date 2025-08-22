package com.ecommerce.productservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.ProductRepositoryPort;
import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
import com.ecommerce.productservice.infrastructure.adapter.persistence.mapper.ProductMapper;
import com.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    //saveAndFlush fixed the createdAt and updatedAt issue
    @Override
    public Product save(Product product) {
        var orderEntity = mapper.toEntity(product);
        var savedEntity = jpaRepository.saveAndFlush(orderEntity);
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
        ProductEntity existingEntity = jpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
        // Update fields from the input Product
        ProductEntity productUpdated = mapper.setEntityFields(existingEntity, product);
        ProductEntity savedEntity = jpaRepository.saveAndFlush(productUpdated);
        log.info("Updated entity: id={}, createdAt={}, updatedAt={}",
                savedEntity.getId(), savedEntity.getCreatedAt(), savedEntity.getUpdatedAt());
        return mapper.toDomain(savedEntity);
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

    @Cacheable("products")
    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids)
                .stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Page<Product> findByColor(String color, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<ProductEntity> entities = jpaRepository.findByColor(color, pageable);
        return new PageImpl<>(entities.getContent().stream().map(mapper::toDomain).toList(),
                pageable, entities.getTotalElements());
    }
}
