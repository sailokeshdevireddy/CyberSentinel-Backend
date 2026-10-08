package com.cybersentinel.repository;

import com.cybersentinel.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByOrganizationId(UUID organizationId);
    Optional<Incident> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByIdAndOrganizationId(UUID id, UUID organizationId);
}
