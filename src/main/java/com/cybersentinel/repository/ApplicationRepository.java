package com.cybersentinel.repository;

import com.cybersentinel.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByOrganizationId(UUID organizationId);
    Optional<Application> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByIdAndOrganizationId(UUID id, UUID organizationId);
}
