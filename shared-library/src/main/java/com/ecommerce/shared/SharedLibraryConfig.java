package com.ecommerce.shared;

import com.ecommerce.shared.exception.ErrorResponseBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedLibraryConfig {
    @Bean
    public ErrorResponseBuilder buildErrorResponse() {
        return new ErrorResponseBuilder();
    }
}
