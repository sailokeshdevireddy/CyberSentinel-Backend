package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.IncidentStatus;
import com.cybersentinel.model.enums.Severity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IncidentDto {
    private UUID id;
    private UUID organizationId;
    private UUID applicationId;
    private String title;
    private Severity severity;
    private Integer riskScore;
    private IncidentStatus status;
    private List<String> relatedThreatIds;
    private List<String> relatedEventIds;
    private UUID assignedToUserId;
    private List<IncidentNoteDto> notes;
    private String resolution;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public List<String> getRelatedThreatIds() { return relatedThreatIds; }
    public void setRelatedThreatIds(List<String> relatedThreatIds) { this.relatedThreatIds = relatedThreatIds; }
    public List<String> getRelatedEventIds() { return relatedEventIds; }
    public void setRelatedEventIds(List<String> relatedEventIds) { this.relatedEventIds = relatedEventIds; }
    public UUID getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(UUID assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    public List<IncidentNoteDto> getNotes() { return notes; }
    public void setNotes(List<IncidentNoteDto> notes) { this.notes = notes; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
