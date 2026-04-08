package com.food.delivery.order_service.service;

import com.food.delivery.order_service.client.CustomerClient;
import com.food.delivery.order_service.client.RestaurantClient;
import com.food.delivery.order_service.dto.request.OrderItemRequest;
import com.food.delivery.order_service.dto.request.PlaceOrderRequest;
import com.food.delivery.order_service.dto.response.OrderResponse;
import com.food.delivery.order_service.event.OrderEventPublisher;
import com.food.delivery.order_service.event.OrderCancelledEvent;
import com.food.delivery.order_service.event.OrderPlacedEvent;
import com.food.delivery.order_service.exception.ResourceNotFoundException;
import com.food.delivery.order_service.exception.ServiceUnavailableException;
import com.food.delivery.order_service.exception.UnauthorizedException;
import com.food.delivery.order_service.model.Order;
import com.food.delivery.order_service.model.OrderItem;
import com.food.delivery.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerService;
    private final RestaurantClient restaurantService;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        CustomerClient customerService,
                        RestaurantClient restaurantService,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest placeOrderRequest) {

        CustomerClient.CustomerResponse customer = customerService.getCustomerByEmail(email);
        if (customer.getMessage() != null){
            throw new ServiceUnavailableException("Customer service is unavailable");
        }
        RestaurantClient.RestaurantResponse restaurant = restaurantService.getRestaurantById(placeOrderRequest.getRestaurantId());

        if(restaurant.getMessage() != null){
            throw new ServiceUnavailableException("Restaurant service is unavailable");
        }
        if (!restaurant.isActive()) {
            throw new IllegalStateException("Restaurant is currently not accepting orders");
        }

        List<RestaurantClient.MenuItemResponse> menu = restaurantService.getMenuItems(restaurant.getId());

        if (menu.isEmpty()) {
            throw new ServiceUnavailableException("Restaurant menu is currently unavailable, please try again later");
        }

        Map<UUID, RestaurantClient.MenuItemResponse> menuMap = menu.stream()
                .collect(Collectors.toMap(RestaurantClient.MenuItemResponse::getMenuItemId, item -> item));

        Order order = Order.builder()
                .customerId(customer.getId())
                .customerEmail(customer.getEmail())
                .customerName(customer.getLastName() + ", " + customer.getFirstName())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .deliveryAddress(placeOrderRequest.getDeliveryAddress() != null
                        ? placeOrderRequest.getDeliveryAddress()
                        : customer.getDeliveryAddress())
                .specialInstructions(placeOrderRequest.getSpecialInstructions())
                .estimatedDeliveryTime(
                        LocalDateTime.now().plusMinutes(restaurant.getEstimatedDeliveryMinutes()))
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : placeOrderRequest.getItems()) {
            RestaurantClient.MenuItemResponse menuItem = menuMap.get(itemReq.getMenuItemId());

            if (menuItem == null) {
                throw new ResourceNotFoundException("Menu item " + itemReq.getMenuItemId() + " not found in this restaurant");
            }
            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item '" + menuItem.getName() + "' is not available");
            }

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getMenuItemId())
                    .itemName(menuItem.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .specialInstructions(itemReq.getSpecialInstructions())
                    .build();

            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderPlaced(new OrderPlacedEvent(
                savedOrder.getOrderId(),
                savedOrder.getCustomerId(),
                savedOrder.getCustomerEmail(),
                savedOrder.getCustomerName(),
                savedOrder.getRestaurantId(),
                savedOrder.getRestaurantName(),
                savedOrder.getDeliveryAddress(),
                savedOrder.getEstimatedDeliveryTime()
        ));
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderResponse.fromEntity(order);
    }

    public boolean isOrderOwner(UUID orderId, String email) {
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomerEmail().equals(email))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(String email) {
        CustomerClient.CustomerResponse customer = customerService.getCustomerByEmail(email);
        if(customer.getMessage() != null){
            throw new ServiceUnavailableException("Customer service is unavailable");
        }
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(UUID restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomerEmail().equals(email)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.PLACED
                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        // MONOLITH: synchronously cancel delivery
//        if (order.getDelivery() != null) {
//            deliveryService.cancelDelivery(order.getDelivery().getId());
//        }

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishOrderCancelled(new OrderCancelledEvent(
                savedOrder.getOrderId(),
                savedOrder.getCustomerId(),
                savedOrder.getRestaurantId(),
                "Cancelled by customer",
                LocalDateTime.now()
        ));

        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Internal workflow: when a customer is deleted, cancel all of their orders.
     * This method publishes {@link OrderCancelledEvent} for each newly-cancelled order.
     */
    @Transactional
    public void cancelOrdersByCustomerId(UUID customerId, String reason) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (orders == null || orders.isEmpty()) {
            return;
        }

        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.CANCELLED || order.getStatus() == Order.OrderStatus.DELIVERED) {
                continue; // idempotency for duplicate events
            }

            order.setStatus(Order.OrderStatus.CANCELLED);
            Order savedOrder = orderRepository.save(order);

            eventPublisher.publishOrderCancelled(new OrderCancelledEvent(
                    savedOrder.getOrderId(),
                    savedOrder.getCustomerId(),
                    savedOrder.getRestaurantId(),
                    reason,
                    LocalDateTime.now()
            ));
        }
    }
}