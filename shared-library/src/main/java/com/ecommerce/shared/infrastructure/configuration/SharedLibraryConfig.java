package com.ecommerce.shared.infrastructure.configuration;

import com.ecommerce.shared.application.exception.ErrorResponseBuilder;
import com.ecommerce.shared.infrastructure.openapi.ServiceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configuration for the shared library, providing beans for error handling and message sources.
 */
@Configuration
@EnableConfigurationProperties(ServiceConfig.class)
public class SharedLibraryConfig {


    /**
     * Creates an ErrorResponseBuilder bean for building standardized error responses.
     *
     * @param messageSource The message source for error messages.
     * @return An ErrorResponseBuilder instance.
     */
    @Bean
    public ErrorResponseBuilder errorResponseBuilder(MessageSource messageSource) {
        return new ErrorResponseBuilder(messageSource);
    }

    /**
     * Configures a MessageSource for retrieving localized error messages.
     *
     * @return A ReloadableResourceBundleMessageSource instance.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(10);
        return messageSource;
    }
}