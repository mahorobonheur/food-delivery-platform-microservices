package com.food.delivery.delivery_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalServiceClientConfig {

    @Bean
    public RequestInterceptor internalTokenInterceptor(
            @Value("${internal.service.token}") String internalToken) {
        return template -> {
            template.header("X-Internal-Token", internalToken);
            template.header("X-Internal-Service", "delivery-service");
        };
    }
}
