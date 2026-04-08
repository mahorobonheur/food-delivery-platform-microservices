package com.food.delivery.customer_service.mapper;

import com.food.delivery.customer_service.dto.response.RoleResponseDto;
import com.food.delivery.customer_service.model.Role;

public class RoleMapper {
    public static RoleResponseDto toDto(Role role) {
        return new RoleResponseDto(role.getRoleId(), role.getRoleName());
    }
}
