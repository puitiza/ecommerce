package com.ecommerce.apigateway;

import com.ecommerce.apigateway.properties.SecurityProperties;
import com.ecommerce.shared.infrastructure.configuration.SharedLibraryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import(SharedLibraryConfig.class) // Import only the needed bean
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
