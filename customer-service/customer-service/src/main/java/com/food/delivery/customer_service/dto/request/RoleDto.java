package com.food.delivery.customer_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoleDto {

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be 3-50 characters")
    String roleName;
}
