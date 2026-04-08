package com.food.delivery.delivery_service.client;

import com.food.delivery.delivery_service.fallback.OrderClientFallbackFactory;
import com.food.delivery.delivery_service.config.InternalServiceClientConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.UUID;

@FeignClient(
        value = "order-service",
        fallbackFactory = OrderClientFallbackFactory.class,
        configuration = InternalServiceClientConfig.class
)
public interface OrderClient {

    @GetMapping("/api/orders/internal/{id}")
    OrderResponse getOrderById(@PathVariable("id") UUID id);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class OrderResponse {
        private UUID id;
        private String status;
        private String deliveryAddress;
        private LocalDateTime estimatedDeliveryTime;
        private UUID customerId;
        private String customerName;
        private UUID restaurantId;
        private String restaurantName;
        private String message;
    }
}
