package com.ecommerce.productservice.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private Integer inventory;

    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> categories;

    @Column(columnDefinition = "json")
    private Object additionalData;

}
