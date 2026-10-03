package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(Role role);
    long countByRole(Role role);
}
