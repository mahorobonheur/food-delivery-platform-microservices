package com.food.delivery.delivery_service.event;

import com.food.delivery.delivery_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDeliveryStatus(DeliveryStatusEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELIVERY_EXCHANGE,
                RabbitMQConfig.DELIVERY_STATUS_KEY,
                event
        );
    }
}
