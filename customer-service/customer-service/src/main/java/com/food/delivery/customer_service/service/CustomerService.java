package com.food.delivery.customer_service.service;

import com.food.delivery.customer_service.dto.request.CustomerDto;
import com.food.delivery.customer_service.exception.DuplicateResourceException;
import com.food.delivery.customer_service.exception.ResourceNotFoundException;
import com.food.delivery.customer_service.model.Customer;
import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.CustomerRepository;
import com.food.delivery.customer_service.repository.RoleRepository;
import com.food.delivery.customer_service.event.CustomerDeletedEvent;
import com.food.delivery.customer_service.event.CustomerEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerEventPublisher customerEventPublisher;

    public CustomerService(CustomerRepository customerRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            CustomerEventPublisher customerEventPublisher) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerEventPublisher = customerEventPublisher;
    }

    @CacheEvict(value = "customersPage", allEntries = true)
    public Customer createUser(CustomerDto dto) {
        if (customerRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("User with this username already exists!");
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User with this email already exists!");
        }

        Role role = roleRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_CUSTOMER not found. Please ensure roles are seeded."));

        Customer customer = new Customer();
        customer.setEmail(dto.getEmail());
        customer.setUsername(dto.getUsername());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setCity(dto.getCity());
        customer.setPhone(dto.getPhone());
        customer.setDeliveryAddress(dto.getDeliveryAddress());
        customer.setRole(role);
        return customerRepository.save(customer);
    }

    @Cacheable(value = "customerById", key = "#customerId")
    public Customer getUserById(UUID customerId){
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional(readOnly = true)
    public Customer findByEmail(String email){
        Customer customer = customerRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        return customer;
    }

    @Cacheable(value = "customersPage", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Customer> getAllUsers(Pageable pageable){
        return customerRepository.findAll(pageable);
    }

    @Transactional
    @CacheEvict(value = {"customerById", "customersPage"}, allEntries = true)
    public void deleteUser(UUID customerId){
        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + customerId+ " is not found"));

        // Publish domain event so other services can react (cancel orders, fail deliveries).
        customerEventPublisher.publishCustomerDeleted(new CustomerDeletedEvent(
                existing.getCustomerId(),
                existing.getEmail(),
                "Customer deleted",
                java.time.LocalDateTime.now()
        ));

        customerRepository.deleteById(customerId);
    }



    @Transactional
    @CachePut(value = "customerById", key = "#userId")
    @CacheEvict(value = "customersPage", allEntries = true)
    public Customer updateUser(UUID userId, CustomerDto userDetails) {
        Customer existingCustomer = customerRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Customer not found!")
        );

        if (!existingCustomer.getEmail().equals(userDetails.getEmail()) &&
                customerRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("Username " + userDetails.getUsername() + " is already taken by another user.");
        }
        Role role = roleRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        existingCustomer.setRole(role);

        existingCustomer.setEmail(userDetails.getEmail());
        existingCustomer.setUsername(userDetails.getUsername());
        existingCustomer.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        existingCustomer.setFirstName(userDetails.getFirstName());
        existingCustomer.setLastName(userDetails.getLastName());
        existingCustomer.setCity(userDetails.getCity());
        existingCustomer.setPhone(userDetails.getPhone());
        existingCustomer.setDeliveryAddress(userDetails.getDeliveryAddress());

        return customerRepository.save(existingCustomer);
    }
}
