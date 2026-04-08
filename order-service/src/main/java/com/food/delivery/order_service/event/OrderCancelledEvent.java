package com.food.delivery.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private String reason;
    private LocalDateTime cancelledAt;
}
