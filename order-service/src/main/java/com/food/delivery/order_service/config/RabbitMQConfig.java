package com.food.delivery.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE     = "order.exchange";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_KEY   = "order.placed";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";

    public static final String CUSTOMER_DELETED_QUEUE = "customer.deleted.queue";
    public static final String CUSTOMER_DELETED_KEY = "customer.deleted";
    public static final String CUSTOMER_DELETED_DLQ = "customer.deleted.dlq";
    public static final String CUSTOMER_DELETED_DLX = "customer.deleted.dlx";

    public static final String DELIVERY_EXCHANGE  = "delivery.exchange";
    public static final String DELIVERY_STATUS_QUEUE = "delivery.status.queue";
    public static final String DELIVERY_STATUS_KEY   = "delivery.status";
    public static final String DELIVERY_STATUS_DLQ = "delivery.status.dlq";
    public static final String DELIVERY_STATUS_DLX = "delivery.status.dlx";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Queue deliveryStatusQueue() {
        return QueueBuilder.durable(DELIVERY_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DELIVERY_STATUS_DLX)
                .withArgument("x-dead-letter-routing-key", DELIVERY_STATUS_DLQ)
                .build();
    }

    @Bean
    public Binding deliveryStatusBinding() {
        return BindingBuilder
                .bind(deliveryStatusQueue())
                .to(deliveryExchange())
                .with(DELIVERY_STATUS_KEY);
    }

    @Bean
    public DirectExchange deliveryDeadLetterExchange() {
        return new DirectExchange(DELIVERY_STATUS_DLX);
    }

    @Bean
    public Queue deliveryDeadLetterQueue() {
        return QueueBuilder.durable(DELIVERY_STATUS_DLQ).build();
    }

    @Bean
    public Binding deliveryDeadLetterBinding() {
        return BindingBuilder
                .bind(deliveryDeadLetterQueue())
                .to(deliveryDeadLetterExchange())
                .with(DELIVERY_STATUS_DLQ);
    }

    @Bean
    public Queue customerDeletedQueue() {
        return QueueBuilder.durable(CUSTOMER_DELETED_QUEUE)
                .withArgument("x-dead-letter-exchange", CUSTOMER_DELETED_DLX)
                .withArgument("x-dead-letter-routing-key", CUSTOMER_DELETED_DLQ)
                .build();
    }

    @Bean
    public Binding customerDeletedBinding() {
        return BindingBuilder
                .bind(customerDeletedQueue())
                .to(orderExchange())
                .with(CUSTOMER_DELETED_KEY);
    }

    @Bean
    public DirectExchange customerDeletedDeadLetterExchange() {
        return new DirectExchange(CUSTOMER_DELETED_DLX);
    }

    @Bean
    public Queue customerDeletedDeadLetterQueue() {
        return QueueBuilder.durable(CUSTOMER_DELETED_DLQ).build();
    }

    @Bean
    public Binding customerDeletedDeadLetterBinding() {
        return BindingBuilder
                .bind(customerDeletedDeadLetterQueue())
                .to(customerDeletedDeadLetterExchange())
                .with(CUSTOMER_DELETED_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

}
