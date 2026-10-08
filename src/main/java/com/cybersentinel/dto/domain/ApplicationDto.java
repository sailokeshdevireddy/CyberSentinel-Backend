package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public class ApplicationDto {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private ApplicationStatus status;
    private String apiKey;
    private String secretMasked;
    private Instant createdAt;
    private Instant lastEventAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getSecretMasked() { return secretMasked; }
    public void setSecretMasked(String secretMasked) { this.secretMasked = secretMasked; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(Instant lastEventAt) { this.lastEventAt = lastEventAt; }
}
