package com.food.delivery.customer_service.service;

import com.food.delivery.customer_service.model.Customer;
import com.food.delivery.customer_service.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomUserDetailService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String roleName = customer.getRole().getRoleName();
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        return User
                .withUsername(customer.getEmail())
                .password(customer.getPassword())
                .authorities(new SimpleGrantedAuthority(authority))
                .build();
    }
}
