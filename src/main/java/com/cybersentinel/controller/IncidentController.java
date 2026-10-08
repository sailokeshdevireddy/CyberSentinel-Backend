package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.IncidentDto;
import com.cybersentinel.dto.domain.IncidentTimelineEntryDto;
import com.cybersentinel.dto.domain.UpdateIncidentStatusRequest;
import com.cybersentinel.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ApiResponse<List<IncidentDto>> getIncidents() {
        return ApiResponse.ok(incidentService.getIncidents());
    }

    @GetMapping("/{id}")
    public ApiResponse<IncidentDto> getIncidentById(@PathVariable UUID id) {
        return ApiResponse.ok(incidentService.getIncidentById(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<IncidentDto> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return ApiResponse.ok(incidentService.updateStatus(id, request));
    }

    @GetMapping("/{id}/timeline")
    public ApiResponse<List<IncidentTimelineEntryDto>> getTimeline(@PathVariable UUID id) {
        return ApiResponse.ok(incidentService.getTimeline(id));
    }
}
