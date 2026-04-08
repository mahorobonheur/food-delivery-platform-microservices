package com.food.delivery.customer_service.config;

import com.food.delivery.customer_service.model.Role;
import com.food.delivery.customer_service.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            List<String> defaultRoles = List.of(
                    "ROLE_CUSTOMER",
                    "ROLE_ADMIN",
                    "ROLE_RESTAURANT_OWNER",
                    "ROLE_DELIVERY_DRIVER"
            );
            for (String roleName : defaultRoles) {
                if (roleRepository.findByRoleName(roleName).isEmpty()) {
                    roleRepository.save(Role.builder().roleName(roleName).build());
                    System.out.println("[DataInitializer] Created role: " + roleName);
                }
            }
        };
    }
}