package com.ecommerce.orderservice.model.dto;

import com.ecommerce.orderservice.model.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an order placed by a user.")
public class OrderDto {

    @Schema(description = "Unique identifier for the order", example = "98765432-1098-cdef-0123-456789012345")
    private String id;

    @Schema(description = "Identifier of the user who placed the order", example = "a5a6d1e9-cc05-4614-b83a-104d17b92d10")
    private String userId;

    @Schema(description = "List of items included in the order")
    //@Valid
    private List<OrderItemDto> items;

    @Schema(description = "Current status of the order", example = "VALIDATION_SUCCEEDED")
    private OrderStatus status;

    @Schema(description = "Date and time when the order was created", example = "2024-01-19T00:18:54.343691")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private String createdAt;

    @Schema(description = "Date and time when the order was last updated", example = "2024-01-19T00:18:54.367833")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private String updatedAt;

    @Schema(description = "Total price of the order, including all items", example = "75.94")
    private BigDecimal totalPrice;

    @Schema(description = "Shipping address for the order", example = "123 Main St, Anytown, CA 12345")
    private String shippingAddress;

}



