package com.food.delivery.customer_service.mapper;

import com.food.delivery.customer_service.dto.response.CustomerResponseDto;
import com.food.delivery.customer_service.model.Customer;

public class CustomerMapper {

    private CustomerMapper() {}

    public static CustomerResponseDto toDto(Customer customer) {
        return CustomerResponseDto.builder()
                .id(customer.getCustomerId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .deliveryAddress(customer.getDeliveryAddress())
                .city(customer.getCity())
                .roleId(customer.getRole().getRoleId())
                .roleName(customer.getRole().getRoleName())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}