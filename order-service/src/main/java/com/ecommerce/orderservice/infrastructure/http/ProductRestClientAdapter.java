package com.ecommerce.orderservice.infrastructure.http;

import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.application.request.OrderItemRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductRestClientAdapter implements ProductServicePort {
    private final RestClient restClient;

    public ProductRestClientAdapter(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    @Override
    public ProductResponse getProductById(Long id, String token) {
        return restClient.get()
                .uri("/products/{id}", id)
                .header("Authorization", token)
                .retrieve()
                .body(ProductResponse.class);
    }

    @Override
    public ProductAvailabilityResponse verifyProductAvailability(OrderItemRequest request, String token) {
        return restClient.post()
                .uri("/products/verify-availability")
                .header("Authorization", token)
                .body(request)
                .retrieve()
                .body(ProductAvailabilityResponse.class);
    }

    @Override
    public void updateProductInventory(Long id, int updatedInventory, String token) {
        restClient.put()
                .uri("/products/update-inventory/{id}/{updatedInventory}", id, updatedInventory)
                .header("Authorization", token)
                .retrieve()
                .toBodilessEntity();
    }
}