package com.food.delivery.order_service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent {
    private UUID orderId;
    private UUID customerId;
    private String customerEmail;
    private String customerName;
    private UUID restaurantId;
    private String restaurantName;
    private String deliveryAddress;
    private LocalDateTime estimatedDeliveryTime;
}