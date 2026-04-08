package com.food.delivery.customer_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RoleResponseDto {
    private UUID roleId;
    private String roleName;
}
