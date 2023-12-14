package com.ecommerce.apigateway.config;


import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes().route("AUTH-SERVICE", r -> r.path("/auth/**").uri("lb://AUTH-SERVICE"))
                .route("PRODUCT-SERVICE", r -> r.path("/product/**").uri("lb://PRODUCT-SERVICE"))
                .route("PAYMENT-SERVICE", r -> r.path("/payment/**").uri("lb://PAYMENT-SERVICE"))
                .route("ORDER-SERVICE", r -> r.path("/order/**").uri("lb://ORDER-SERVICE")).build();
    }
}
