package com.food.delivery.delivery_service.service;

import com.food.delivery.delivery_service.client.OrderClient;
import com.food.delivery.delivery_service.dto.DeliveryResponse;
import com.food.delivery.delivery_service.event.DeliveryStatusEvent;
import com.food.delivery.delivery_service.event.OrderEventPublisher;
import com.food.delivery.delivery_service.event.OrderPlacedEvent;
import com.food.delivery.delivery_service.exception.ResourceNotFoundException;
import com.food.delivery.delivery_service.model.Delivery;
import com.food.delivery.delivery_service.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final OrderEventPublisher eventPublisher;
    private final OrderClient orderClient;

    private static final String[] DRIVERS = {
            "Carlos Martinez", "Sarah Johnson", "Mike Chen", "Priya Patel", "James Wilson"
    };
    private static final String[] PHONES = {
            "+1-555-0101", "+1-555-0102", "+1-555-0103", "+1-555-0104", "+1-555-0105"
    };

    public DeliveryService(DeliveryRepository deliveryRepository,
                           OrderEventPublisher eventPublisher,
                           OrderClient orderClient) {
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
        this.orderClient = orderClient;
    }

    @Transactional
    public void createDeliveryForOrder(OrderPlacedEvent event) {
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Delivery already exists for order {}, skipping duplicate event", event.getOrderId());
            return;
        }

        int driverIndex = (int) (Math.random() * DRIVERS.length);
        OrderClient.OrderResponse order = orderClient.getOrderById(event.getOrderId());

        UUID customerId = event.getCustomerId();
        String customerName = event.getCustomerName();
        UUID restaurantId = event.getRestaurantId();
        String restaurantName = event.getRestaurantName();
        String deliveryAddress = event.getDeliveryAddress();
        String orderStatus = "PLACED";

        if (order != null && order.getMessage() == null) {
            customerId = order.getCustomerId();
            customerName = order.getCustomerName();
            restaurantId = order.getRestaurantId();
            restaurantName = order.getRestaurantName();
            deliveryAddress = order.getDeliveryAddress();
            orderStatus = order.getStatus();
        } else {
            log.warn("Falling back to event payload for order {}", event.getOrderId());
        }

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(customerId)
                .customerName(customerName)
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .orderStatus(orderStatus)
                .status(Delivery.DeliveryStatus.ASSIGNED)
                .driverName(DRIVERS[driverIndex])
                .driverPhone(PHONES[driverIndex])
                .deliveryAddress(deliveryAddress)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        eventPublisher.publishDeliveryStatus(new DeliveryStatusEvent(
                event.getOrderId(),
                saved.getDeliveryId(),
                "ASSIGNED",
                DRIVERS[driverIndex],
                PHONES[driverIndex]
        ));

        log.info("Delivery assigned to {} for order {}", DRIVERS[driverIndex], event.getOrderId());
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId.toString()));
        return DeliveryResponse.fromEntity(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return DeliveryResponse.fromEntity(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getByStatus(String status) {
        Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        return deliveryRepository.findByStatus(deliveryStatus)
                .stream().map(DeliveryResponse::fromEntity).toList();
    }

    @Transactional
    public DeliveryResponse updateStatus(UUID deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        Delivery.DeliveryStatus newStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        switch (newStatus) {
            case PICKED_UP -> delivery.setPickedUpAt(LocalDateTime.now());
            case DELIVERED -> delivery.setDeliveredAt(LocalDateTime.now());
            default -> {}
        }

        Delivery saved = deliveryRepository.save(delivery);

        eventPublisher.publishDeliveryStatus(new DeliveryStatusEvent(
                saved.getOrderId(),
                saved.getDeliveryId(),
                newStatus.name(),
                saved.getDriverName(),
                saved.getDriverPhone()
        ));

        log.info("Delivery {} status updated to {}", deliveryId, newStatus);

        return DeliveryResponse.fromEntity(saved);
    }

    @Transactional
    public void cancelDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus(Delivery.DeliveryStatus.FAILED);
        Delivery saved = deliveryRepository.save(delivery);

        eventPublisher.publishDeliveryStatus(new DeliveryStatusEvent(
                saved.getOrderId(),
                saved.getDeliveryId(),
                "FAILED",
                saved.getDriverName(),
                saved.getDriverPhone()
        ));

        log.info("Delivery {} cancelled", deliveryId);
    }

    @Transactional
    public void cancelDeliveryByOrderId(UUID orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresentOrElse(delivery -> {
            delivery.setStatus(Delivery.DeliveryStatus.FAILED);
            Delivery saved = deliveryRepository.save(delivery);

            eventPublisher.publishDeliveryStatus(new DeliveryStatusEvent(
                    saved.getOrderId(),
                    saved.getDeliveryId(),
                    "FAILED",
                    saved.getDriverName(),
                    saved.getDriverPhone()
            ));
            log.info("Delivery cancelled for order {}", orderId);
        }, () -> log.warn("No delivery found for cancelled order {}", orderId));
    }
}