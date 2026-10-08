package com.cybersentinel.repository;

import com.cybersentinel.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {
    List<SecurityEvent> findByOrganizationId(UUID organizationId);
    Optional<SecurityEvent> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
