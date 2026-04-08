package com.food.delivery.customer_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponseDto {

    private UUID id;

    private String username;
    private String email;

    private String firstName;
    private String lastName;

    private String phone;
    private String deliveryAddress;
    private String city;

    private UUID roleId;
    private String roleName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}