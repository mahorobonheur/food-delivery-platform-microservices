package com.food.delivery.restaurant_service.service;

import com.food.delivery.restaurant_service.client.CustomerClient;
import com.food.delivery.restaurant_service.dto.request.MenuItemRequest;
import com.food.delivery.restaurant_service.dto.request.RestaurantRequest;
import com.food.delivery.restaurant_service.dto.response.MenuItemResponse;
import com.food.delivery.restaurant_service.dto.response.RestaurantResponse;
import com.food.delivery.restaurant_service.model.MenuItem;
import com.food.delivery.restaurant_service.model.Restaurant;
import com.food.delivery.restaurant_service.repository.MenuItemRepository;
import com.food.delivery.restaurant_service.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private RestaurantService restaurantService;

    private UUID restaurantId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restaurantId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateRestaurant() {

        RestaurantRequest request = new RestaurantRequest();
        request.setName("Test Restaurant");
        request.setCity("Kigali");

        CustomerClient.CustomerResponse owner = new CustomerClient.CustomerResponse();
        owner.setId(ownerId);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        when(customerClient.getCustomerByEmail("john@email.com"))
                .thenReturn(owner);

        Restaurant savedRestaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .name("Test Restaurant")
                .city("Kigali")
                .ownerId(ownerId)
                .build();

        when(restaurantRepository.save(any(Restaurant.class)))
                .thenReturn(savedRestaurant);

        RestaurantResponse response = restaurantService.createRestaurant("john@email.com", request);

        assertNotNull(response);
        assertEquals("Test Restaurant", response.getName());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void shouldGetRestaurantById() {

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .ownerId(ownerId)
                .name("Restaurant A")
                .build();

        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(restaurant));

        CustomerClient.CustomerResponse owner = new CustomerClient.CustomerResponse();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        when(customerClient.getCustomerById(ownerId)).thenReturn(owner);

        RestaurantResponse response = restaurantService.getById(restaurantId);

        assertNotNull(response);
        assertEquals("Restaurant A", response.getName());
    }

    @Test
    void shouldAddMenuItem() {

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .ownerId(ownerId)
                .build();

        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(restaurant));

        CustomerClient.CustomerResponse owner = new CustomerClient.CustomerResponse();
        owner.setEmail("owner@email.com");

        when(customerClient.getCustomerById(ownerId))
                .thenReturn(owner);

        MenuItemRequest request = new MenuItemRequest();
        request.setName("Pizza");
        request.setPrice(BigDecimal.valueOf(10));

        MenuItem savedItem = MenuItem.builder()
                .menuItemId(UUID.randomUUID())
                .name("Pizza")
                .price(BigDecimal.valueOf(10))
                .restaurant(restaurant)
                .build();

        when(menuItemRepository.save(any(MenuItem.class)))
                .thenReturn(savedItem);

        MenuItemResponse response =
                restaurantService.addMenuItem(restaurantId, "owner@email.com", request);

        assertNotNull(response);
        assertEquals("Pizza", response.getName());
    }

    @Test
    void shouldToggleMenuItemAvailability() {

        MenuItem item = MenuItem.builder()
                .menuItemId(UUID.randomUUID())
                .available(true)
                .restaurant(Restaurant.builder()
                        .ownerId(ownerId)
                        .build())
                .build();

        when(menuItemRepository.findById(item.getMenuItemId()))
                .thenReturn(Optional.of(item));

        CustomerClient.CustomerResponse owner = new CustomerClient.CustomerResponse();
        owner.setEmail("owner@email.com");

        when(customerClient.getCustomerById(ownerId))
                .thenReturn(owner);

        restaurantService.toggleMenuItemAvailability(item.getMenuItemId(), "owner@email.com");

        verify(menuItemRepository).save(item);
        assertFalse(item.isAvailable());
    }

}