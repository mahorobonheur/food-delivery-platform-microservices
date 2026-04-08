package com.food.delivery.customer_service.service;

import com.food.delivery.customer_service.dto.request.RoleDto;
import com.food.delivery.customer_service.exception.DuplicateResourceException;
import com.food.delivery.customer_service.exception.ResourceNotFoundException;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;


    @Test
    void shouldReturnRoleByIdSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();
        role.setRoleId(roleId);
        role.setRoleName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        Role result = roleService.getRoleById(roleId);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleName());
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFoundById() {
        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roleService.getRoleById(roleId));
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName("USER");

        when(roleRepository.existsByRoleName("USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Role result = roleService.createRole(roleDto);

        assertNotNull(result);
        assertEquals("USER", result.getRoleName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleAlreadyExists() {
        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName("ADMIN");

        when(roleRepository.existsByRoleName("ADMIN")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> roleService.createRole(roleDto));

        verify(roleRepository, never()).save(any());
    }


    @Test
    void shouldReturnPagedRoles() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Role> rolePage = new PageImpl<>(
                List.of(new Role(), new Role())
        );

        when(roleRepository.findAll(pageable)).thenReturn(rolePage);

        Page<Role> result = roleService.getRoles(pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void shouldUpdateRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();

        Role existingRole = new Role();
        existingRole.setRoleId(roleId);
        existingRole.setRoleName("USER");

        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName("ADMIN");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(existingRole)).thenReturn(existingRole);

        Role result = roleService.updateRole(roleId, roleDto);

        assertEquals("ADMIN", result.getRoleName());
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        UUID roleId = UUID.randomUUID();
        Role role = new Role();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        roleService.deleteRole(roleId);

        verify(roleRepository).delete(role);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingRole() {
        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roleService.deleteRole(roleId));
    }
}