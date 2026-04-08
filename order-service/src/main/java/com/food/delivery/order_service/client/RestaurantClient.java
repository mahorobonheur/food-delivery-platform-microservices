package com.food.delivery.order_service.client;

import com.food.delivery.order_service.fallback.RestaurantClientFallbackFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(value = "restaurant-service", fallbackFactory = RestaurantClientFallbackFactory.class)
public interface RestaurantClient {

    @GetMapping("/api/restaurants/{id}")
    RestaurantResponse getRestaurantById(@PathVariable("id") UUID id);

    @GetMapping("/api/restaurants/name")
    RestaurantResponse getRestaurantByName(@RequestParam String name);

    @GetMapping("/api/restaurants/{id}/menu")
    List<MenuItemResponse> getMenuItems(@PathVariable("id") UUID restaurantId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class RestaurantResponse {
        private UUID id;
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String city;
        private String phone;
        private boolean active;
        private double rating;
        private int estimatedDeliveryMinutes;
        private int menuItemCount;

        private UUID ownerId;
        private String ownerName;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class MenuItemResponse{
        private UUID menuItemId;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private boolean available;
        private String imageUrl;
        private UUID restaurantId;
        private String restaurantName;
    }

}
