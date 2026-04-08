package com.food.delivery.order_service.fallback;

import com.food.delivery.order_service.client.RestaurantClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RestaurantClientFallbackFactory implements FallbackFactory<RestaurantClient> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantClientFallbackFactory.class);

    @Override
    public RestaurantClient create(Throwable cause) {
        logger.error("Restaurant service error: {}", cause.getMessage());
        return new RestaurantClient() {
            @Override
            public RestaurantResponse getRestaurantById(UUID id) {
                return RestaurantResponse.builder()
                        .id(id)
                        .message("Restaurant service is currently unavailable")
                        .build();
            }

            @Override
            public RestaurantResponse getRestaurantByName(String name) {
                return RestaurantResponse.builder()
                        .name(name)
                        .message("Restaurant service is currently unavailable")
                        .build();
            }

            @Override
            public List<MenuItemResponse> getMenuItems(UUID restaurantId) {
                logger.warn("Fallback triggered for getMenuItems, restaurantId: {}", restaurantId);
                return List.of();
            }
        };
    }
}
