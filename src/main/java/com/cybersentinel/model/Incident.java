package com.cybersentinel.model;

import com.cybersentinel.model.enums.IncidentStatus;
import com.cybersentinel.model.enums.Severity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "incidents")
@EntityListeners(AuditingEntityListener.class)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID organizationId;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "incident_related_threats",
            joinColumns = @JoinColumn(name = "incident_id", referencedColumnName = "id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"incident_id", "threat_id"})
    )
    @Column(name = "threat_id", columnDefinition = "BINARY(16)", nullable = false)
    private List<UUID> relatedThreatIds;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "incident_related_events",
            joinColumns = @JoinColumn(name = "incident_id", referencedColumnName = "id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"incident_id", "event_id"})
    )
    @Column(name = "event_id", columnDefinition = "BINARY(16)", nullable = false)
    private List<UUID> relatedEventIds;

    @Column(columnDefinition = "BINARY(16)")
    private UUID assignedToUserId;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
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
    public List<UUID> getRelatedThreatIds() { return relatedThreatIds; }
    public void setRelatedThreatIds(List<UUID> relatedThreatIds) { this.relatedThreatIds = relatedThreatIds; }
    public List<UUID> getRelatedEventIds() { return relatedEventIds; }
    public void setRelatedEventIds(List<UUID> relatedEventIds) { this.relatedEventIds = relatedEventIds; }
    public UUID getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(UUID assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
