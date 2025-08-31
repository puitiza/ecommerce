package com.ecommerce.productservice.domain.port.in;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.productservice.domain.exception.DuplicateProductNameException;
import com.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Defines the application service interface for product-related business logic.
 * Acts as an inbound port in Clean Architecture, orchestrating domain operations.
 */
public interface ProductUseCase {

    /**
     * Creates a new product and persists it to the database.
     *
     * @param request the product details
     * @return the created product's details
     * @throws DuplicateProductNameException if the product name already exists
     */
    ProductResponse create(ProductRequest request);

    /**
     * Retrieves all products with pagination.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @return a paginated list of products
     */
    Page<ProductResponse> findAllPaginated(int page, int size);

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product's details
     * @throws ResourceNotFoundException if the product is not found
     */
    ProductResponse findById(Long id);

    /**
     * Updates an existing product.
     *
     * @param id      the product ID
     * @param request the updated product details
     * @return the updated product's details
     * @throws DuplicateProductNameException if the new name already exists
     * @throws ResourceNotFoundException     if the product is not found
     */
    ProductResponse update(Long id, ProductRequest request);

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID
     * @throws ResourceNotFoundException if the product is not found
     */
    void delete(Long id);

    /**
     * Verifies the availability of a batch of products by checking inventory levels.
     *
     * @param request the batch request containing product IDs and quantities
     * @return a response with availability details for each product
     */
    ProductBatchValidationResponse verifyAndGetProducts(ProductBatchValidationRequest request);

    /**
     * Retrieves details for a batch of products.
     *
     * @param request the request containing product IDs
     * @return a list of product details or errors for non-existent products
     */
    List<ProductBatchDetailsResponse> findProductDetails(ProductBatchDetailsRequest request);

    /**
     * Retrieves products filtered by color with pagination.
     *
     * @param color the color to filter by
     * @param page  the page number (0-based)
     * @param size  the number of items per page
     * @return a paginated list of products matching the color
     */
    Page<ProductResponse> findByColor(String color, int page, int size);
}