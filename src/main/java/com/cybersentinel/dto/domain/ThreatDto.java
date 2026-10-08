package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.Severity;
import com.cybersentinel.model.enums.ThreatStatus;
import com.cybersentinel.model.enums.ThreatType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ThreatDto {
    private UUID id;
    private UUID organizationId;
    private UUID applicationId;
    private ThreatType type;
    private Severity severity;
    private Integer riskScore;
    private ThreatStatus status;
    private List<String> relatedEventIds;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public ThreatType getType() { return type; }
    public void setType(ThreatType type) { this.type = type; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public ThreatStatus getStatus() { return status; }
    public void setStatus(ThreatStatus status) { this.status = status; }
    public List<String> getRelatedEventIds() { return relatedEventIds; }
    public void setRelatedEventIds(List<String> relatedEventIds) { this.relatedEventIds = relatedEventIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
