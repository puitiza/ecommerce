package com.ecommerce.userservice.model.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="permit-urls")
public class PermitUrlsProperties {

    private String[] swagger;

    private String[] users;

}
