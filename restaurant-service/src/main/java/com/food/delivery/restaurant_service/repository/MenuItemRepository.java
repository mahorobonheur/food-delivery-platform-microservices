package com.food.delivery.restaurant_service.repository;

import com.food.delivery.restaurant_service.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurantRestaurantIdAndAvailableTrue(UUID restaurantId);
    List<MenuItem> findByRestaurantRestaurantId(UUID restaurantId);
    List<MenuItem> findByRestaurantRestaurantIdAndCategory(UUID restaurantId, String category);
}

