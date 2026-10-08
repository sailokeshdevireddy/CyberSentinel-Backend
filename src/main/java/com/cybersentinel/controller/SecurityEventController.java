package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.CreateSecurityEventRequest;
import com.cybersentinel.dto.domain.SecurityEventDto;
import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.service.EventProcessingService;
import com.cybersentinel.service.SecurityEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private final SecurityEventService securityEventService;
    private final EventProcessingService eventProcessingService;

    public SecurityEventController(SecurityEventService securityEventService,
                                   EventProcessingService eventProcessingService) {
        this.securityEventService = securityEventService;
        this.eventProcessingService = eventProcessingService;
    }

    @GetMapping
    public ApiResponse<List<SecurityEventDto>> getEvents() {
        return ApiResponse.ok(securityEventService.getEvents());
    }

    @GetMapping("/{id}")
    public ApiResponse<SecurityEventDto> getEventById(@PathVariable UUID id) {
        return ApiResponse.ok(securityEventService.getEventById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SecurityEventDto> ingestEvent(@RequestHeader("X-Application-Id") String applicationId,
                                                     @RequestHeader("X-Application-Secret") String applicationSecret,
                                                     @Valid @RequestBody CreateSecurityEventRequest request) {
        SecurityEvent processedEvent = eventProcessingService.processEvent(applicationId, applicationSecret, request);
        return ApiResponse.ok(securityEventService.mapToDto(processedEvent));
    }
}
