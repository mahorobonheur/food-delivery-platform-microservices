package com.food.delivery.customer_service.controller;

import com.food.delivery.customer_service.config.JwtUtil;
import com.food.delivery.customer_service.dto.request.CustomerDto;
import com.food.delivery.customer_service.dto.request.LoginRequestDto;
import com.food.delivery.customer_service.dto.response.CustomerResponseDto;
import com.food.delivery.customer_service.dto.response.JwtResponse;
import com.food.delivery.customer_service.mapper.CustomerMapper;
import com.food.delivery.customer_service.model.Customer;
import com.food.delivery.customer_service.service.CustomerService;
import com.food.delivery.customer_service.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final TokenService tokenService;
    private final JwtUtil  jwtUtil;
    private final AuthenticationManager authenticationManager;

    public CustomerController(CustomerService customerService, TokenService tokenService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.customerService = customerService;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Create a new customer with the provided details")
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @Valid @RequestBody CustomerDto customerDto) {

        Customer customer = customerService.createUser(customerDto);
        return new ResponseEntity<>(
                CustomerMapper.toDto(customer),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Login a customer", description = "Login a customer with the provided email and password")
    public JwtResponse login(@RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request){
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        token.setDetails(new WebAuthenticationDetails(request));

        authenticationManager.authenticate(token);

        Customer user = customerService.findByEmail(loginRequestDto.getEmail());
        String jwt = jwtUtil.generateToken(user);
        return new JwtResponse(jwt, "Bearer", user.getEmail(), user.getRole().getRoleName());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout a customer", description = "Logout a customer by revoking their token")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokenService.revokeToken(token);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieve a customer by their username")
    public ResponseEntity<CustomerResponseDto> findByEmail(@PathVariable String email) {
        Customer customer = customerService.findByEmail(email);
        return ResponseEntity.ok(CustomerMapper.toDto(customer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a customer by their ID")
    public ResponseEntity<CustomerResponseDto> getCustomerById(
            @PathVariable UUID id) {

        Customer customer = customerService.getUserById(id);
        return ResponseEntity.ok(CustomerMapper.toDto(customer));
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve a list of all customers")
    public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(
            Pageable pageable) {

        Page<CustomerResponseDto> page =
                customerService.getAllUsers(pageable)
                        .map(CustomerMapper::toDto);

        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update an existing customer with the provided details")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDto customerDto) {

        Customer updatedCustomer =
                customerService.updateUser(id, customerDto);

        return ResponseEntity.ok(CustomerMapper.toDto(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Delete a customer by their ID")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable UUID id) {

        customerService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}