package com.cybersentinel.service;

import com.cybersentinel.dto.domain.IncidentDto;
import com.cybersentinel.dto.domain.IncidentNoteDto;
import com.cybersentinel.dto.domain.IncidentTimelineEntryDto;
import com.cybersentinel.dto.domain.UpdateIncidentStatusRequest;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Incident;
import com.cybersentinel.model.IncidentNote;
import com.cybersentinel.model.IncidentTimelineEntry;
import com.cybersentinel.model.enums.ActorType;
import com.cybersentinel.model.enums.IncidentStatus;
import com.cybersentinel.model.enums.TimelineEventType;
import com.cybersentinel.repository.IncidentNoteRepository;
import com.cybersentinel.repository.IncidentRepository;
import com.cybersentinel.repository.IncidentTimelineEntryRepository;
import com.cybersentinel.security.TenantContext;
import com.cybersentinel.security.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentNoteRepository noteRepository;
    private final IncidentTimelineEntryRepository timelineRepository;
    private final TenantContext tenantContext;
    private final TenantGuard tenantGuard;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentNoteRepository noteRepository,
                           IncidentTimelineEntryRepository timelineRepository,
                           TenantContext tenantContext,
                           TenantGuard tenantGuard) {
        this.incidentRepository = incidentRepository;
        this.noteRepository = noteRepository;
        this.timelineRepository = timelineRepository;
        this.tenantContext = tenantContext;
        this.tenantGuard = tenantGuard;
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> getIncidents() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        return incidentRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(UUID id) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        Incident incident = incidentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> ApiException.notFound("Incident not found"));
        return mapToDto(incident);
    }

    @Transactional
    public IncidentDto updateStatus(UUID id, UpdateIncidentStatusRequest request) {
        tenantGuard.verifyIncidentAccess(id);
        Incident incident = incidentRepository.findById(id).orElseThrow();
        
        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(request.getStatus());
        incident = incidentRepository.save(incident);

        IncidentTimelineEntry timelineEntry = new IncidentTimelineEntry();
        timelineEntry.setIncidentId(id);
        timelineEntry.setType(TimelineEventType.STATUS_CHANGED);
        timelineEntry.setDescription("Status changed from " + oldStatus + " to " + request.getStatus());
        timelineEntry.setActorType(ActorType.USER);
        timelineEntry.setActorName("User " + tenantContext.getCurrentUserId());
        timelineEntry.setOccurredAt(Instant.now());
        timelineRepository.save(timelineEntry);

        return mapToDto(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentTimelineEntryDto> getTimeline(UUID id) {
        tenantGuard.verifyIncidentAccess(id);
        return timelineRepository.findByIncidentId(id).stream()
                .map(this::mapTimelineToDto)
                .collect(Collectors.toList());
    }

    private IncidentDto mapToDto(Incident incident) {
        IncidentDto dto = new IncidentDto();
        dto.setId(incident.getId());
        dto.setOrganizationId(incident.getOrganizationId());
        dto.setApplicationId(incident.getApplicationId());
        dto.setTitle(incident.getTitle());
        dto.setSeverity(incident.getSeverity());
        dto.setRiskScore(incident.getRiskScore());
        dto.setStatus(incident.getStatus());
        
        if (incident.getRelatedThreatIds() != null) {
            dto.setRelatedThreatIds(incident.getRelatedThreatIds().stream()
                    .map(UUID::toString).collect(Collectors.toList()));
        }
        if (incident.getRelatedEventIds() != null) {
            dto.setRelatedEventIds(incident.getRelatedEventIds().stream()
                    .map(UUID::toString).collect(Collectors.toList()));
        }
        
        dto.setAssignedToUserId(incident.getAssignedToUserId());
        dto.setResolution(incident.getResolution());
        dto.setCreatedAt(incident.getCreatedAt());
        dto.setUpdatedAt(incident.getUpdatedAt());

        List<IncidentNoteDto> notes = noteRepository.findByIncidentId(incident.getId()).stream()
                .map(this::mapNoteToDto)
                .collect(Collectors.toList());
        dto.setNotes(notes);

        return dto;
    }

    private IncidentNoteDto mapNoteToDto(IncidentNote note) {
        IncidentNoteDto dto = new IncidentNoteDto();
        dto.setId(note.getId());
        dto.setAuthorId(note.getAuthorId());
        dto.setAuthorName(note.getAuthorName());
        dto.setNote(note.getNote());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }

    private IncidentTimelineEntryDto mapTimelineToDto(IncidentTimelineEntry entry) {
        IncidentTimelineEntryDto dto = new IncidentTimelineEntryDto();
        dto.setId(entry.getId());
        dto.setIncidentId(entry.getIncidentId());
        dto.setType(entry.getType());
        dto.setDescription(entry.getDescription());
        dto.setActorType(entry.getActorType());
        dto.setActorName(entry.getActorName());
        dto.setOccurredAt(entry.getOccurredAt());
        return dto;
    }
}
