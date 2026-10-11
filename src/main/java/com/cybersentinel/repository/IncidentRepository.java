package com.cybersentinel.repository;

import com.cybersentinel.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cybersentinel.model.enums.IncidentStatus;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByOrganizationId(UUID organizationId);
    Optional<Incident> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT i FROM Incident i JOIN i.relatedThreatIds t WHERE i.status = :status AND t = :threatId")
    List<Incident> findByStatusAndRelatedThreatId(@Param("status") IncidentStatus status, @Param("threatId") UUID threatId);
}
