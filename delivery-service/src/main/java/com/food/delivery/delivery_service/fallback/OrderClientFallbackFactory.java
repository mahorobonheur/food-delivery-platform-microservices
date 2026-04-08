package com.food.delivery.delivery_service.fallback;

import com.food.delivery.delivery_service.client.OrderClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {
        return id -> OrderClient.OrderResponse.builder()
                .id(id)
                .message("Order service is unavailable")
                .build();
    }
}
