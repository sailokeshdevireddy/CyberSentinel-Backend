package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.ActorType;
import com.cybersentinel.model.enums.TimelineEventType;
import java.time.Instant;
import java.util.UUID;

public class IncidentTimelineEntryDto {
    private UUID id;
    private UUID incidentId;
    private TimelineEventType type;
    private String description;
    private ActorType actorType;
    private String actorName;
    private Instant occurredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }
    public TimelineEventType getType() { return type; }
    public void setType(TimelineEventType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
