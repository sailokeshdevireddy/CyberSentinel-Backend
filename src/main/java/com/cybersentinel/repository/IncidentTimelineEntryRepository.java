package com.cybersentinel.repository;

import com.cybersentinel.model.IncidentTimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;

public interface IncidentTimelineEntryRepository extends JpaRepository<IncidentTimelineEntry, UUID> {
    List<IncidentTimelineEntry> findByIncidentId(UUID incidentId);
}
