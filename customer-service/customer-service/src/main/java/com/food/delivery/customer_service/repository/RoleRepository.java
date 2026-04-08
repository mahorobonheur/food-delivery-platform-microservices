package com.food.delivery.customer_service.repository;

import com.food.delivery.customer_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByRoleName(String name);

    Optional<Role> findByRoleName(String name);
}
