package com.ecommerce.userservice.configuration.openApi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@OpenAPIDefinition
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service APIs")
                        .description("This lists all the User Service API Calls.")
                        .version("v1.0"))
                .servers(serverList());
    }

    private List<Server> serverList() {
        Server localServer = new Server();
        localServer.setDescription("API Gateway URL");
        localServer.setUrl("http://localhost:8090");

        // Add other servers if needed
        Server localServer_2 = new Server();
        localServer_2.setDescription("Direct Local Service URL");
        localServer_2.setUrl("http://localhost:8082");
        return Arrays.asList(localServer, localServer_2);
    }
}
