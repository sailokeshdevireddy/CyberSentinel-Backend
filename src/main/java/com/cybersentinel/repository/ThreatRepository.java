package com.cybersentinel.repository;

import com.cybersentinel.model.Threat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThreatRepository extends JpaRepository<Threat, UUID> {
    List<Threat> findByOrganizationId(UUID organizationId);
    Optional<Threat> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT DISTINCT t FROM Threat t JOIN t.relatedEventIds e WHERE t.status = :status AND t.type = :type AND t.applicationId = :appId AND e IN :eventIds")
    List<Threat> findRelatedOpenThreats(
            @Param("status") com.cybersentinel.model.enums.ThreatStatus status,
            @Param("type") com.cybersentinel.model.enums.ThreatType type,
            @Param("appId") UUID appId,
            @Param("eventIds") List<UUID> eventIds);
}
