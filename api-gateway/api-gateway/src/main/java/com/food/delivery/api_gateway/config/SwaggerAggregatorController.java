package com.food.delivery.api_gateway.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api-docs")
public class SwaggerAggregatorController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{service}")
    public ResponseEntity<String> getSwaggerJson(@PathVariable String service) {
        String url = switch (service) {
            case "customers" -> "http://localhost:8081/v3/api-docs";
            case "restaurants" -> "http://localhost:8082/v3/api-docs";
            case "orders" -> "http://localhost:8083/v3/api-docs";
            default -> "";
        };

        if (url.isEmpty()) return ResponseEntity.notFound().build();

        String swaggerJson = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(swaggerJson);
    }
}