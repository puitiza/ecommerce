package com.ecommerce.shared;

import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.openapi.ServiceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;


@EnableConfigurationProperties(ServiceConfig.class)
@Configuration
public class SharedLibraryConfig {
    @Bean
    public ErrorResponseBuilder buildErrorResponse(MessageSource messageSource) {
        return new ErrorResponseBuilder(messageSource);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(10); // Recharge every 10 seconds
        return messageSource;
    }
}
