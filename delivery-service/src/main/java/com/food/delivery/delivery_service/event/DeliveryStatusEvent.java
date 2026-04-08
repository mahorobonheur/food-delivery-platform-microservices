package com.food.delivery.delivery_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryStatusEvent {
    private UUID orderId;
    private UUID deliveryId;
    private String status;
    private String driverName;
    private String driverPhone;
}