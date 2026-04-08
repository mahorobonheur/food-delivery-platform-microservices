package com.food.delivery.order_service.client;

import com.food.delivery.order_service.fallback.CustomerClientFallbackFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.UUID;

@FeignClient(value = "customer-service", fallbackFactory = CustomerClientFallbackFactory.class)
public interface CustomerClient {


    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable("id") UUID id);

    @GetMapping("/api/customers/email/{email}")
    CustomerResponse getCustomerByEmail(@PathVariable String email);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CustomerResponse{
        private UUID id;

        private String username;
        private String email;

        private String firstName;
        private String lastName;

        private String phone;
        private String deliveryAddress;
        private String city;

        private UUID roleId;
        private String roleName;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private String message;
    }
}
