package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateIncidentStatusRequest {
    @NotNull(message = "status is required")
    private IncidentStatus status;

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
}
