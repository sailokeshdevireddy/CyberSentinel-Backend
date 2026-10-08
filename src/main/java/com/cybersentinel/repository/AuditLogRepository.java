package com.cybersentinel.repository;

import com.cybersentinel.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByOrganizationId(UUID organizationId);
    Optional<AuditLog> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
