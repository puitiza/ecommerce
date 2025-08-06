package com.ecommerce.orderservice.infrastructure.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(LoadBalancerClient loadBalancerClient) {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    var serviceInstance = loadBalancerClient.choose("product-service");
                    if (serviceInstance == null) {
                        throw new IllegalStateException("Product service not found in registry");
                    }
                    request.getHeaders().set("Host", serviceInstance.getHost() + ":" + serviceInstance.getPort());
                    return execution.execute(request, body);
                })
                .baseUrl("http://product-service")
                .build();
    }

    @Bean
    public RestClient paymentRestClient(LoadBalancerClient loadBalancerClient) {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    var serviceInstance = loadBalancerClient.choose("payment-service");
                    if (serviceInstance == null) {
                        throw new IllegalStateException("Payment service not found in registry");
                    }
                    request.getHeaders().set("Host", serviceInstance.getHost() + ":" + serviceInstance.getPort());
                    return execution.execute(request, body);
                })
                .baseUrl("http://payment-service")
                .build();
    }
}
