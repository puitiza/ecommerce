package com.ecommerce.productservice.configuration.openapi;

import com.ecommerce.shared.openapi.OpenApiConfigBase;
import com.ecommerce.shared.openapi.ServiceConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@OpenAPIDefinition
public class OpenApiConfig extends OpenApiConfigBase {

    private final ServiceConfig serviceConfig;

    public OpenApiConfig(ServiceConfig serviceConfig) {
        super(serviceConfig);
        this.serviceConfig = serviceConfig;
    }

    @Bean
    public OpenAPI openApi() {
        return super.openApi();
    }

    @Override
    protected Info getInfo() {
        return new Info()
                .title(serviceConfig.title())
                .description(serviceConfig.description())
                .version(serviceConfig.version());
    }

    @Override
    protected List<Server> getServers() {
        return serviceConfig.servers().stream()
                .map(ServiceConfig.ServerConfig::toServer)
                .toList();
    }
}