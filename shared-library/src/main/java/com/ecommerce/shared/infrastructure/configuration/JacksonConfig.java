package com.ecommerce.shared.infrastructure.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global Jackson configuration for all microservices.
 * <p>
 * This class configures a custom {@link ObjectMapper} to handle the serialization
 * and deserialization of {@link LocalDateTime} objects consistently across the application.
 * This ensures data integrity when communicating with external services (e.g., Feign clients)
 * and processing events from internal systems (e.g., Kafka)
 * and applies {@link SerializationFeature} and {@link DeserializationFeature}
 * add @JsonIgnoreProperties(ignoreUnknown = true) globally for DTOs.
 * but does not affect database operations such as timestamp persistence (handled by Hibernate).
 */
@Configuration
public class JacksonConfig {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Configures a custom {@link ObjectMapper} bean.
     * <p>
     * It registers a {@link JavaTimeModule} to apply a specific format for {@link LocalDateTime}
     * objects, using the {@code DATE_TIME_FORMATTER}. This standardization prevents
     * inconsistencies and errors related to date formats in JSON payloads.
     * <p></p>
     * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
     * to ensure that deserialization does not fail if the  Service adds new fields
     * to its response.
     * and disables FAIL_ON_UNKNOWN_PROPERTIES to ensure resilience against new fields in external service responses.
     *
     * @return A custom configured ObjectMapper instance.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Configure LocalDateTime serialization
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        mapper.registerModule(module);
        // Enable ignoreUnknown for all DTOs
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}