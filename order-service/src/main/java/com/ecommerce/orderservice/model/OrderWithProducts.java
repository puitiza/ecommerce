package com.ecommerce.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderWithProducts {
    private Order order;
    private List<Product> products;
}
