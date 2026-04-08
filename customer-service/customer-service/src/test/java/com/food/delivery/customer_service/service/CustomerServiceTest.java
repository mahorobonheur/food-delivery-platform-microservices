package com.food.delivery.customer_service.service;


import com.food.delivery.customer_service.dto.request.CustomerDto;
import com.food.delivery.customer_service.exception.DuplicateResourceException;
import com.food.delivery.customer_service.exception.ResourceNotFoundException;
import com.food.delivery.customer_service.model.Customer;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.CustomerRepository;
import com.food.delivery.customer_service.repository.RoleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomerRepository customerRepository;


    @Test
    void shouldCreateCustomerSuccessfully() {
        UUID roleId = UUID.randomUUID();
        CustomerDto dto = new CustomerDto();
        dto.setUsername("john_doe");
        dto.setEmail("john@email.com");
        dto.setPassword("password");

        when(customerRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(customerRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.createUser(dto);
        Assertions.assertNotNull(result);
        assertEquals("john_doe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        CustomerDto dto = new CustomerDto();
        dto.setUsername("john");
        dto.setEmail("john@email.com");

        when(customerRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> customerService.createUser(dto));

        verify(customerRepository, never()).save(any());
    }


    @Test
    void shouldThrowExceptionWhenEmailExists() {
        CustomerDto dto = new CustomerDto();
        dto.setUsername("john");
        dto.setEmail("john@email.com");

        when(customerRepository.existsByUsername("john")).thenReturn(false);
        when(customerRepository.existsByEmail("john@email.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> customerService.createUser(dto));
    }

    @Test
    void shouldReturnCustomerById() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setCustomerId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = customerService.getUserById(id);

        assertEquals(id, result.getCustomerId());
    }

    @Test
    void shouldThrowWhenCustomerNotFoundById() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.getUserById(id));
    }

    @Test
    void shouldFindCustomerByEmail() {
        String email = "test@email.com";
        Customer customer = new Customer();
        customer.setEmail(email);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        Customer result = customerService.findByEmail(email);

        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        UUID id = UUID.randomUUID();

        when(customerRepository.existsById(id)).thenReturn(true);

        customerService.deleteUser(id);

        verify(customerRepository).deleteById(id);
    }

    @Test
    void shouldThrowWhenDeletingNonExistingCustomer() {
        UUID id = UUID.randomUUID();

        when(customerRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.deleteUser(id));
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        Customer existing = new Customer();
        existing.setCustomerId(userId);
        existing.setEmail("old@email.com");

        CustomerDto dto = new CustomerDto();
        dto.setEmail("new@email.com");
        dto.setUsername("newUsername");
        dto.setPassword("newPass");

        when(customerRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(new Role()));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.updateUser(userId, dto);

        assertEquals("newUsername", result.getUsername());
        assertEquals("encoded", result.getPassword());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingUser() {
        UUID userId = UUID.randomUUID();

        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateUser(userId, new CustomerDto()));
    }

}
