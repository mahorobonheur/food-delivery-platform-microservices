package com.food.delivery.order_service.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {
    @NotNull
    private UUID menuItemId;
    @Positive
    private int quantity;
    private String specialInstructions;
}