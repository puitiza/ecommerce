package com.ecommerce.productservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Double price;

    private Integer inventory;

    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> categories;

    @Column(columnDefinition = "json")
    private Object additionalData;

}
