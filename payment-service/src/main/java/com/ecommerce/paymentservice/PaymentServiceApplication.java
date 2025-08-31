package com.ecommerce.paymentservice;

import com.ecommerce.shared.infrastructure.configuration.OpenApiConfigBase;
import com.ecommerce.shared.infrastructure.configuration.SharedLibraryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@SpringBootApplication
@Import({SharedLibraryConfig.class, OpenApiConfigBase.class})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
