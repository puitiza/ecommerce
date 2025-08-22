package com.ecommerce.productservice;

import com.ecommerce.shared.infrastructure.configuration.JacksonConfig;
import com.ecommerce.shared.infrastructure.configuration.OpenApiConfigBase;
import com.ecommerce.shared.infrastructure.configuration.SharedLibraryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import({SharedLibraryConfig.class, OpenApiConfigBase.class, JacksonConfig.class})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
