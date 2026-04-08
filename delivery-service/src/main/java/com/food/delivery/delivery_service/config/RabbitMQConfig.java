package com.food.delivery.delivery_service.config;

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
    public static final String ORDER_EXCHANGE      = "order.exchange";
    public static final String ORDER_PLACED_QUEUE  = "order.placed.queue";
    public static final String ORDER_PLACED_KEY    = "order.placed";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";

    public static final String DELIVERY_EXCHANGE   = "delivery.exchange";
    public static final String DELIVERY_STATUS_KEY = "delivery.status";

    public static final String ORDER_PLACED_DLQ = "order.placed.dlq";
    public static final String ORDER_PLACED_DLX = "order.placed.dlx";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return new TopicExchange(DELIVERY_EXCHANGE);
    }

    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_PLACED_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_PLACED_DLQ)
                .withArgument("x-message-ttl", 600000)   // 10 minutes
                .withArgument("x-max-length", 1000)
                .build();
    }

    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_KEY);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(ORDER_PLACED_DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(ORDER_PLACED_DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(ORDER_PLACED_DLQ);
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