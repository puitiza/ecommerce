package com.ecommerce.userservice;

import com.ecommerce.shared.SharedLibraryConfig;
import com.ecommerce.shared.openapi.OpenApiConfigBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import({SharedLibraryConfig.class, OpenApiConfigBase.class})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
