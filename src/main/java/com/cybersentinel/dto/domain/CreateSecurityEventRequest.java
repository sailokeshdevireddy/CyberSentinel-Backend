package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.EventType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public class CreateSecurityEventRequest {
    @NotNull(message = "eventType is required")
    private EventType eventType;
    
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
    
    private String userIdentifier;
    private String device;
    private String ipAddress;
    private String location;
    private Map<String, Object> metadata;

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
