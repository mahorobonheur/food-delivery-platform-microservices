package com.food.delivery.customer_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDeletedEvent {
    private UUID customerId;
    private String email;
    private String reason;
    private LocalDateTime deletedAt;
}

