package com.food.delivery.order_service.event;

import com.food.delivery.order_service.config.RabbitMQConfig;
import com.food.delivery.order_service.model.Order;
import com.food.delivery.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeliveryStatusListener {
    private static final Logger log = LoggerFactory.getLogger(DeliveryStatusListener.class);

    private final OrderRepository orderRepository;

    public DeliveryStatusListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_STATUS_QUEUE)
    @Transactional
    public void handleDeliveryStatusUpdated(DeliveryStatusEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            Order.OrderStatus nextStatus = mapStatus(event.getStatus());
            if (nextStatus != null && order.getStatus() != Order.OrderStatus.CANCELLED) {
                order.setStatus(nextStatus);
                orderRepository.save(order);
                log.info("Order {} updated from delivery event status {}", order.getOrderId(), event.getStatus());
            }
        }, () -> log.warn("Order {} not found for delivery status event", event.getOrderId()));
    }

    private Order.OrderStatus mapStatus(String deliveryStatus) {
        if (deliveryStatus == null) {
            return null;
        }
        return switch (deliveryStatus.toUpperCase()) {
            case "ASSIGNED" -> Order.OrderStatus.CONFIRMED;
            case "PICKED_UP" -> Order.OrderStatus.OUT_FOR_DELIVERY;
            case "DELIVERED" -> Order.OrderStatus.DELIVERED;
            case "FAILED" -> Order.OrderStatus.CANCELLED;
            default -> null;
        };
    }
}
