package com.cybersentinel.repository;

import com.cybersentinel.model.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

import java.time.Instant;
import com.cybersentinel.model.enums.EventType;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {
    List<SecurityEvent> findByOrganizationId(UUID organizationId);
    Optional<SecurityEvent> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<SecurityEvent> findByEventTypeAndUserIdentifierAndIpAddressAndApplicationIdAndOccurredAtGreaterThanEqual(
            EventType eventType, String userIdentifier, String ipAddress, UUID applicationId, Instant occurredAt);
}
