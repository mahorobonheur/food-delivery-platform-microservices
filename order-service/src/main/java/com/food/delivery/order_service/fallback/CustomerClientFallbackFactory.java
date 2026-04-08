package com.food.delivery.order_service.fallback;

import com.food.delivery.order_service.client.CustomerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class CustomerClientFallbackFactory implements FallbackFactory<CustomerClient> {

    private static final Logger logger = LoggerFactory.getLogger(CustomerClientFallbackFactory.class);

    @Override
    public CustomerClient create(Throwable cause) {
        logger.error("Customer service error: {}", cause.getMessage());
        return new CustomerClient() {
            @Override
            public CustomerResponse getCustomerById(UUID id) {
                return CustomerResponse.builder()
                        .id(id)
                        .message("Customer service is currently unavailable")
                        .build();
            }

            @Override
            public CustomerResponse getCustomerByEmail(String email) {
                return CustomerResponse.builder()
                        .email(email)
                        .message("Customer service is currently unavailable")
                        .build();
            }
        };
    }
}
