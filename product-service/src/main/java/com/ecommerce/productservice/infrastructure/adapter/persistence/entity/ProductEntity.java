package com.ecommerce.productservice.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing a product in the database.
 * <p>
 * The {@code @DynamicUpdate} annotation optimizes SQL UPDATE statements by
 * including only the columns that have changed, which is useful for
 * entities with many fields.
 */
@Getter
@Setter
@DynamicUpdate
@Entity(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private Integer inventory;

    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> categories;

    /**
     * Maps a Map<String, Object> to a JSONB column in PostgreSQL.This allows for storing flexible, schemaless additional data.
     * The {@code @JdbcTypeCode(SqlTypes.JSON)} annotation handles the conversion between the Java Map and the
     * PostgreSQL JSONB type.
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> additionalData;

    /**
     * The date and time when the product was first created.
     * Automatically set by Hibernate upon insertion.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The date and time when the product was last updated.
     * Automatically updated by Hibernate on any entity modification.
     */
    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    /**
     * Version field for optimistic locking to prevent concurrent modifications.
     */
    @Version
    private Long version;
}