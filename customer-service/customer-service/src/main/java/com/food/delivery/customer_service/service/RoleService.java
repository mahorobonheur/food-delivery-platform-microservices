package com.food.delivery.customer_service.service;

import com.food.delivery.customer_service.dto.request.RoleDto;
import com.food.delivery.customer_service.exception.DuplicateResourceException;
import com.food.delivery.customer_service.exception.ResourceNotFoundException;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class RoleService {
    private RoleRepository roleRepository;
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId).orElseThrow(() ->
                new ResourceNotFoundException("Role not found"));
    }

    public Role createRole(RoleDto roleDto) {
        if(roleRepository.existsByRoleName(roleDto.getRoleName())){
            throw new DuplicateResourceException("Role already exists");
        }
        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        return roleRepository.save(role);
    }

    public Page<Role> getRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public Role updateRole(UUID roleId, RoleDto roleDto) {
        Role role = getRoleById(roleId);
        role.setRoleName(roleDto.getRoleName());
        return roleRepository.save(role);
    }

    public void deleteRole(UUID roleId) {
        Role role = getRoleById(roleId);
        roleRepository.delete(role);
    }

}
