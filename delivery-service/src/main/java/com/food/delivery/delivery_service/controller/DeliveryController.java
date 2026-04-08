package com.food.delivery.delivery_service.controller;

import com.food.delivery.delivery_service.dto.DeliveryResponse;
import com.food.delivery.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable UUID deliveryId) {
        return ResponseEntity.ok(deliveryService.getById(deliveryId));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<DeliveryResponse> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getByStatus(status));
    }

    @PatchMapping("/{deliveryId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable UUID deliveryId, @RequestParam String status) {
        return ResponseEntity.ok(deliveryService.updateStatus(deliveryId, status));
    }

    @PostMapping("/{deliveryId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable UUID deliveryId) {
        deliveryService.cancelDelivery(deliveryId);
        return ResponseEntity.noContent().build();
    }
}