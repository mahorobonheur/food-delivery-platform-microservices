package com.food.delivery.order_service.event;

import com.food.delivery.order_service.config.RabbitMQConfig;
import com.food.delivery.order_service.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerDeletedEventListener {

    private final OrderService orderService;

    public CustomerDeletedEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_DELETED_QUEUE)
    public void handleCustomerDeleted(CustomerDeletedEvent event) {
        orderService.cancelOrdersByCustomerId(
                event.getCustomerId(),
                event.getReason()
        );
    }
}

