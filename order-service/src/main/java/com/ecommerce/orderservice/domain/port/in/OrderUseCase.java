package com.ecommerce.orderservice.domain.port.in;

import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Inbound port defining the use cases for order management.
 * This interface is implemented by the application layer.
 */
public interface OrderUseCase {

    /**
     * Creates a new order.
     *
     * @param request The order creation request.
     * @return The created order response.
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * Retrieves all orders with pagination.
     *
     * @param page The page number (0-based).
     * @param size The page size.
     * @return A paginated list of order responses.
     */
    Page<OrderResponse> getAllOrders(int page, int size);

    /**
     * Retrieves an order by its ID.
     *
     * @param id The order ID.
     * @return The order response.
     */
    OrderResponse getOrderById(UUID id);

    /**
     * Updates an existing order.
     *
     * @param id      The order ID.
     * @param request The updated order details.
     * @return The updated order response.
     */
    OrderResponse updateOrder(UUID id, OrderRequest request);

    /**
     * Cancels an order.
     *
     * @param id The order ID.
     */
    void cancelOrder(UUID id);

    void deleteOrder(UUID id);

    OrderResponse confirmOrder(UUID id);
}