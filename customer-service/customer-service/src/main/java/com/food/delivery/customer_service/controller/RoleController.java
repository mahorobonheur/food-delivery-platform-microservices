package com.food.delivery.customer_service.controller;

import com.food.delivery.customer_service.dto.request.RoleDto;
import com.food.delivery.customer_service.dto.response.RoleResponseDto;
import com.food.delivery.customer_service.mapper.RoleMapper;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @PostMapping
    @Operation(summary = "Add a new role", description = "Create a new role with the provided details")
    public ResponseEntity<RoleResponseDto> addRole(@Valid  @RequestBody RoleDto roleDto) {
        Role role = roleService.createRole(roleDto);
        return ResponseEntity.ok(RoleMapper.toDto(role));
        }

        @GetMapping
        @Operation(summary = "Get all roles", description = "Retrieve a list of all roles")
        public ResponseEntity<Page<RoleResponseDto>> getAllRoles(Pageable pageable) {

            Page<RoleResponseDto> roles = roleService.getRoles(pageable).map(RoleMapper::toDto);
            return ResponseEntity.ok(roles);
        }

        @GetMapping("/{roleId}")
        @Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
        public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID roleId) {
        Role role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(RoleMapper.toDto(role));
        }

        @PutMapping("/{roleId}")
        @Operation(summary = "Update role", description = "Update an existing role with the provided details")
        public ResponseEntity<RoleResponseDto> updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleDto roleDto) {
        Role role = roleService.updateRole(roleId, roleDto);
        return ResponseEntity.ok(RoleMapper.toDto(role));
        }

        @DeleteMapping("/{roleId}")
        @Operation(summary = "Delete role", description = "Delete a role by its ID")
        public void deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
        }
}
