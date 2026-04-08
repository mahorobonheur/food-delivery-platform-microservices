package com.food.delivery.restaurant_service.service;

import com.food.delivery.restaurant_service.client.CustomerClient;
import com.food.delivery.restaurant_service.dto.request.MenuItemRequest;
import com.food.delivery.restaurant_service.dto.request.RestaurantRequest;
import com.food.delivery.restaurant_service.dto.response.MenuItemResponse;
import com.food.delivery.restaurant_service.dto.response.RestaurantResponse;
import com.food.delivery.restaurant_service.exception.ResourceNotFoundException;
import com.food.delivery.restaurant_service.exception.ServiceUnavailableException;
import com.food.delivery.restaurant_service.exception.UnauthorizedException;
import com.food.delivery.restaurant_service.model.MenuItem;
import com.food.delivery.restaurant_service.model.Restaurant;
import com.food.delivery.restaurant_service.repository.MenuItemRepository;
import com.food.delivery.restaurant_service.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerClient customerClient;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository,
                             CustomerClient customerClient) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerClient = customerClient;
    }

    private void validateCustomerResponse(CustomerClient.CustomerResponse owner, String identifier) {
        if (owner.getMessage() != null) {
            if (owner.getMessage().contains("404") || owner.getMessage().contains("not found")) {
                throw new ResourceNotFoundException("Customer " + identifier + " not found");
            }
            throw new ServiceUnavailableException("Customer Service not available");
        }
    }

    @Transactional
    public RestaurantResponse createRestaurant(String ownerEmail, RestaurantRequest request) {
        CustomerClient.CustomerResponse owner = customerClient.getCustomerByEmail(ownerEmail);
        validateCustomerResponse(owner, ownerEmail);

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .cuisineType(request.getCuisineType())
                .address(request.getAddress())
                .city(request.getCity())
                .phone(request.getPhone())
                .estimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes())
                .ownerId(owner.getId())
                .build();

        restaurant = restaurantRepository.save(restaurant);

        RestaurantResponse dto = RestaurantResponse.fromEntity(restaurant);
        dto.setOwnerName(owner.getFirstName() + " " + owner.getLastName());

        return dto;
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getById(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant " + restaurantId + " not found"));

        RestaurantResponse dto = RestaurantResponse.fromEntity(restaurant);

        CustomerClient.CustomerResponse owner = customerClient.getCustomerById(restaurant.getOwnerId());
        validateCustomerResponse(owner, restaurant.getOwnerId().toString());
        dto.setOwnerName(owner.getFirstName() + " " + owner.getLastName());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCity(String city) {
        return restaurantRepository.findByCityIgnoreCaseAndActiveTrue(city)
                .stream()
                .map(r -> {
                    RestaurantResponse dto = RestaurantResponse.fromEntity(r);
                    CustomerClient.CustomerResponse owner = customerClient.getCustomerById(r.getOwnerId());
                    validateCustomerResponse(owner, r.getOwnerId().toString());
                    dto.setOwnerName(owner.getFirstName() + " " + owner.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(cuisineType)
                .stream()
                .map(r -> {
                    RestaurantResponse dto = RestaurantResponse.fromEntity(r);
                    CustomerClient.CustomerResponse owner = customerClient.getCustomerById(r.getOwnerId());
                    validateCustomerResponse(owner, r.getOwnerId().toString());
                    dto.setOwnerName(owner.getFirstName() + " " + owner.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActive() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(r -> {
                    RestaurantResponse dto = RestaurantResponse.fromEntity(r);
                    CustomerClient.CustomerResponse owner = customerClient.getCustomerById(r.getOwnerId());
                    validateCustomerResponse(owner, r.getOwnerId().toString());
                    dto.setOwnerName(owner.getFirstName() + " " + owner.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse addMenuItem(UUID restaurantId, String email, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant " + restaurantId + " not found"));

        CustomerClient.CustomerResponse owner = customerClient.getCustomerById(restaurant.getOwnerId());
        validateCustomerResponse(owner, restaurant.getOwnerId().toString());

        if (!owner.getEmail().equals(email)) {
            throw new UnauthorizedException("You don't own this restaurant");
        }

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();

        return MenuItemResponse.fromEntity(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenu(UUID restaurantId) {
        return menuItemRepository.findByRestaurantRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(MenuItemResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse updateMenuItem(UUID itemId, String ownerEmail, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem " + itemId + " not found"));

        CustomerClient.CustomerResponse owner = customerClient.getCustomerById(item.getRestaurant().getOwnerId());
        validateCustomerResponse(owner, ownerEmail);

        if (!owner.getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't own this restaurant");
        }

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());

        return MenuItemResponse.fromEntity(menuItemRepository.save(item));
    }

    @Transactional
    public void toggleMenuItemAvailability(UUID itemId, String ownerEmail) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem " + itemId + " not found"));

        CustomerClient.CustomerResponse owner = customerClient.getCustomerById(item.getRestaurant().getOwnerId());
        validateCustomerResponse(owner, ownerEmail);

        if (!owner.getEmail().equals(ownerEmail)) {
            throw new UnauthorizedException("You don't own this restaurant");
        }

        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
    }

    public Restaurant findEntityById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant " + id + " not found"));
    }

    public MenuItem findMenuItemById(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem " + id + " not found"));
    }
}