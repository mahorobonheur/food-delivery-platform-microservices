package com.food.delivery.restaurant_service.dto.response;

import com.food.delivery.restaurant_service.model.Restaurant;
import lombok.Data;

import java.util.UUID;

@Data
public class RestaurantResponse {
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

    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        RestaurantResponse dto = new RestaurantResponse();
        dto.setId(restaurant.getRestaurantId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisineType(restaurant.getCuisineType());
        dto.setAddress(restaurant.getAddress());
        dto.setCity(restaurant.getCity());
        dto.setPhone(restaurant.getPhone());
        dto.setActive(restaurant.isActive());
        dto.setRating(restaurant.getRating());
        dto.setEstimatedDeliveryMinutes(restaurant.getEstimatedDeliveryMinutes());
        dto.setMenuItemCount(restaurant.getMenuItems() != null ? restaurant.getMenuItems().size() : 0);
        dto.setOwnerId(restaurant.getOwnerId());
        return dto;
    }
}