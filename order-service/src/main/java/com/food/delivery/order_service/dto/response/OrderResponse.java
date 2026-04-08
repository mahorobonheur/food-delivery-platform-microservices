package com.food.delivery.order_service.dto.response;

import com.food.delivery.order_service.model.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private String deliveryAddress;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDeliveryTime;

    private UUID customerId;
    private String customerName;
    private UUID restaurantId;
    private String restaurantName;

    private UUID deliveryId;

    private List<OrderItemDetail> items;

    @Data
    public static class OrderItemDetail {
        private UUID id;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    public static OrderResponse fromEntity(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.setId(order.getOrderId());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setSpecialInstructions(order.getSpecialInstructions());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());

        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setRestaurantId(order.getRestaurantId());
        dto.setRestaurantName(order.getRestaurantName());


        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemDetail detail = new OrderItemDetail();
            detail.setId(item.getOrderItemId());
            detail.setItemName(item.getItemName());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());
            detail.setSubtotal(item.getSubtotal());
            return detail;
        }).toList());

        return dto;
    }
}