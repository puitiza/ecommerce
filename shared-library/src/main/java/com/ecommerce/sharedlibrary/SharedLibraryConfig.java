package com.ecommerce.sharedlibrary;

import com.ecommerce.sharedlibrary.exception.BuildErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedLibraryConfig {
    @Bean
    public BuildErrorResponse buildErrorResponse() {
        return new BuildErrorResponse();
    }
}
