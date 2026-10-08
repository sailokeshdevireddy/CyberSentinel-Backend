package com.cybersentinel.repository;

import com.cybersentinel.model.IncidentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;

public interface IncidentNoteRepository extends JpaRepository<IncidentNote, UUID> {
    List<IncidentNote> findByIncidentId(UUID incidentId);
}
