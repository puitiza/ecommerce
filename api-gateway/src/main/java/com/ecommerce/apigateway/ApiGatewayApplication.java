package com.ecommerce.apigateway;

import com.ecommerce.apigateway.properties.SecurityProperties;
import com.ecommerce.shared.infrastructure.configuration.SharedLibraryConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import(SharedLibraryConfig.class) // Import only the needed bean
@OpenAPIDefinition(info = @Info(title = "API Gateway", version = "1.0", description = "Documentation API Gateway v1.0"))
@EnableConfigurationProperties(SecurityProperties.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
