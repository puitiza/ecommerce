package com.ecommerce.productservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.productservice.infrastructure.adapter.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Query(value = "SELECT * FROM products p WHERE p.additional_data ->> 'color' = :color", nativeQuery = true)
    Page<ProductEntity> findByColor(@Param("color") String color, Pageable pageable);

    boolean existsByName(String name);

    Optional<ProductEntity> findByName(String name);

    List<ProductEntity> findAllByIdIn(List<Long> ids);
}
