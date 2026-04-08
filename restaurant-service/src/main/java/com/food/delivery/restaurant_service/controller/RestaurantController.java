package com.food.delivery.restaurant_service.controller;

import com.food.delivery.restaurant_service.dto.request.MenuItemRequest;
import com.food.delivery.restaurant_service.dto.request.RestaurantRequest;
import com.food.delivery.restaurant_service.dto.response.MenuItemResponse;
import com.food.delivery.restaurant_service.dto.response.RestaurantResponse;
import com.food.delivery.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }


    @GetMapping("/search/city/{city}")
    public ResponseEntity<List<RestaurantResponse>> searchByCity(@PathVariable String city) {
        return ResponseEntity.ok(restaurantService.searchByCity(city));
    }

    @GetMapping("/search/cuisine/{type}")
    public ResponseEntity<List<RestaurantResponse>> searchByCuisine(@PathVariable String type) {
        return ResponseEntity.ok(restaurantService.searchByCuisine(type));
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<RestaurantResponse>> getAllActive() {
        return ResponseEntity.ok(restaurantService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable UUID id) {

        return ResponseEntity.ok().body(restaurantService.getMenu(id));
    }


    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            Authentication auth, @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(auth.getName(), request));
    }

    @PostMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable UUID restaurantId,
            Authentication auth,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.addMenuItem(restaurantId, auth.getName(), request));
    }

    @PutMapping("/menu/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable UUID itemId,
            Authentication auth,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(itemId, auth.getName(), request));
    }

    @PatchMapping("/menu/{itemId}/toggle")
    public ResponseEntity<Void> toggleAvailability(
            @PathVariable UUID itemId, Authentication auth) {
        restaurantService.toggleMenuItemAvailability(itemId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}