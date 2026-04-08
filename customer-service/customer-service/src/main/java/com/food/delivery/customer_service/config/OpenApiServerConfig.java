package com.food.delivery.customer_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiServerConfig {

    public static final String BEARER_JWT = "bearer-jwt";
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description(
                                "Use **Authorize** and paste the access token from login (Bearer is applied automatically). "
                                        + "Roles such as CUSTOMER and ADMIN are enforced from the JWT.")
                        .version("1.0"))
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }
}

