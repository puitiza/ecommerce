package com.ecommerce.orderservice.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(Map<String, List<String>> permitUrls) {
}
