package com.ecommerce.orderservice;

import com.ecommerce.orderservice.infrastructure.properties.SecurityProperties;
import com.ecommerce.shared.infrastructure.configuration.SharedLibraryConfig;
import com.ecommerce.shared.infrastructure.configuration.OpenApiConfigBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
@Import({SharedLibraryConfig.class, OpenApiConfigBase.class})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
