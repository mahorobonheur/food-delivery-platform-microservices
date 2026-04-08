package com.food.delivery.delivery_service.repository;

import com.food.delivery.delivery_service.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrderId(UUID orderId);
    List<Delivery> findByStatus(Delivery.DeliveryStatus status);
    List<Delivery> findByDriverNameIgnoreCase(String driverName);
}
