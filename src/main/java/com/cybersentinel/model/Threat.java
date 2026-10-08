package com.cybersentinel.model;

import com.cybersentinel.model.enums.Severity;
import com.cybersentinel.model.enums.ThreatStatus;
import com.cybersentinel.model.enums.ThreatType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "threats")
@EntityListeners(AuditingEntityListener.class)
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID organizationId;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "threat_related_events",
            joinColumns = @JoinColumn(name = "threat_id", referencedColumnName = "id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"threat_id", "event_id"})
    )
    @Column(name = "event_id", columnDefinition = "BINARY(16)", nullable = false)
    private List<UUID> relatedEventIds;

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
    public ThreatType getType() { return type; }
    public void setType(ThreatType type) { this.type = type; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public ThreatStatus getStatus() { return status; }
    public void setStatus(ThreatStatus status) { this.status = status; }
    public List<UUID> getRelatedEventIds() { return relatedEventIds; }
    public void setRelatedEventIds(List<UUID> relatedEventIds) { this.relatedEventIds = relatedEventIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
