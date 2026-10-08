package com.cybersentinel.model;

import com.cybersentinel.model.enums.AuditResult;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID organizationId;

    @Column(columnDefinition = "BINARY(16)")
    private UUID actorId;

    @Column(nullable = false)
    private String actorName;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(length = 100, nullable = false)
    private String targetType;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditResult result;

    @Column(length = 45, nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Instant occurredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
