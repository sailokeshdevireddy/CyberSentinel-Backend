package com.cybersentinel.repository;

import com.cybersentinel.model.Threat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

public interface ThreatRepository extends JpaRepository<Threat, UUID> {
    List<Threat> findByOrganizationId(UUID organizationId);
    Optional<Threat> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
