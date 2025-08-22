package com.ecommerce.productservice.domain.port.out;

import com.ecommerce.productservice.domain.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);

    Product findById(Long id);

    Page<Product> findAll(int page, int size);

    void delete(Long id);

    Product update(Long id, Product product);

    boolean existsByName(String name);

    Optional<Product> findByName(String name);

    List<Product> findAllByIds(List<Long> ids);

    Page<Product> findByColor(String color, int page, int size);
}