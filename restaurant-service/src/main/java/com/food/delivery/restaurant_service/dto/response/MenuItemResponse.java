package com.food.delivery.restaurant_service.dto.response;

import com.food.delivery.restaurant_service.model.MenuItem;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class MenuItemResponse {
    private UUID menuItemId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean available;
    private String imageUrl;
    private UUID restaurantId;
    private String restaurantName;

    public static MenuItemResponse fromEntity(MenuItem m) {
        MenuItemResponse dto = new MenuItemResponse();
        dto.setMenuItemId(m.getMenuItemId());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setPrice(m.getPrice());
        dto.setCategory(m.getCategory());
        dto.setAvailable(m.isAvailable());
        dto.setImageUrl(m.getImageUrl());
        dto.setRestaurantId(m.getRestaurant().getMenuItems());
        dto.setRestaurantName(m.getRestaurant().getName());
        return dto;
    }

    private void setRestaurantId(List<MenuItem> menuItems) {
    }


}