package com.food.delivery.delivery_service.event;

import com.food.delivery.delivery_service.config.RabbitMQConfig;
import com.food.delivery.delivery_service.service.DeliveryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final DeliveryService deliveryService;

    public OrderEventListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        deliveryService.createDeliveryForOrder(event);
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        deliveryService.cancelDeliveryByOrderId(event.getOrderId());
    }
}