package com.food.delivery.delivery_service.dto;

import com.food.delivery.delivery_service.model.Delivery;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DeliveryResponse {
    private UUID id;
    private String status;
    private String driverName;
    private String driverPhone;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;


    private UUID orderId;
    private String orderStatus;
    private UUID customerId;
    private String customerName;
    private String restaurantName;

    public static DeliveryResponse fromEntity(Delivery delivery) {
        DeliveryResponse deliveryResponse = new DeliveryResponse();
        deliveryResponse.setId(delivery.getDeliveryId());
        deliveryResponse.setStatus(delivery.getStatus().name());
        deliveryResponse.setDriverName(delivery.getDriverName());
        deliveryResponse.setDriverPhone(delivery.getDriverPhone());
        deliveryResponse.setPickupAddress(delivery.getPickupAddress());
        deliveryResponse.setDeliveryAddress(delivery.getDeliveryAddress());
        deliveryResponse.setAssignedAt(delivery.getAssignedAt());
        deliveryResponse.setPickedUpAt(delivery.getPickedUpAt());
        deliveryResponse.setDeliveredAt(delivery.getDeliveredAt());
        deliveryResponse.setCreatedAt(delivery.getCreatedAt());

        deliveryResponse.setOrderId(delivery.getOrderId());
        deliveryResponse.setOrderStatus(delivery.getOrderStatus());
        deliveryResponse.setCustomerId(delivery.getCustomerId());
        deliveryResponse.setCustomerName(delivery.getCustomerName());
        deliveryResponse.setRestaurantName(delivery.getRestaurantName());
        return deliveryResponse;
    }
}
