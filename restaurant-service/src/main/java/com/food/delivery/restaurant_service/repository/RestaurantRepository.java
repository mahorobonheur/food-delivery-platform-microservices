package com.food.delivery.restaurant_service.repository;

import com.food.delivery.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    List<Restaurant> findByActiveTrue();
    List<Restaurant> findByCityIgnoreCaseAndActiveTrue(String city);
    List<Restaurant> findByCuisineTypeIgnoreCaseAndActiveTrue(String cuisineType);
    List<Restaurant> findByOwnerId(UUID ownerId);
}
