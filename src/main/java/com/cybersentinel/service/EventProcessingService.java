package com.cybersentinel.service;

import com.cybersentinel.dto.domain.CreateSecurityEventRequest;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Application;
import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.model.enums.EventStatus;
import com.cybersentinel.repository.ApplicationRepository;
import com.cybersentinel.repository.SecurityEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventProcessingService {

    private final ApplicationRepository applicationRepository;
    private final SecurityEventRepository securityEventRepository;
    private final ApiCredentialService apiCredentialService;
    private final RiskEngine riskEngine;
    private final ThreatDetector threatDetector;
    private final IncidentFactory incidentFactory;
    private final ObjectMapper objectMapper;

    public EventProcessingService(ApplicationRepository applicationRepository,
                                  SecurityEventRepository securityEventRepository,
                                  ApiCredentialService apiCredentialService,
                                  RiskEngine riskEngine,
                                  ThreatDetector threatDetector,
                                  IncidentFactory incidentFactory,
                                  ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.securityEventRepository = securityEventRepository;
        this.apiCredentialService = apiCredentialService;
        this.riskEngine = riskEngine;
        this.threatDetector = threatDetector;
        this.incidentFactory = incidentFactory;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SecurityEvent processEvent(String appIdStr, String appSecret, CreateSecurityEventRequest request) {
        UUID appId;
        try {
            appId = UUID.fromString(appIdStr);
        } catch (IllegalArgumentException e) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Invalid Application ID format");
        }

        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> ApiException.unauthorized("UNAUTHORIZED", "Application not found"));

        if (!apiCredentialService.verifySecret(appSecret, app.getSecretHash())) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Invalid Application Secret");
        }

        SecurityEvent event = new SecurityEvent();
        event.setOrganizationId(app.getOrganizationId());
        event.setApplicationId(app.getId());
        event.setEventType(request.getEventType());
        event.setOccurredAt(request.getOccurredAt());
        event.setReceivedAt(Instant.now());
        event.setUserIdentifier(request.getUserIdentifier());
        event.setDevice(request.getDevice());
        event.setIpAddress(request.getIpAddress());
        event.setLocation(request.getLocation());
        event.setStatus(EventStatus.RECEIVED);

        if (request.getMetadata() != null) {
            try {
                event.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (JsonProcessingException e) {
                // ignore or handle
            }
        }

        // 1. Risk Engine
        riskEngine.evaluateEvent(event);
        
        // 2. Save Event
        event.setStatus(EventStatus.ANALYZED);
        event = securityEventRepository.save(event);
        
        // Update application lastEventAt
        app.setLastEventAt(Instant.now());
        applicationRepository.save(app);

        // 3. Threat Detector
        threatDetector.analyze(event);

        // 4. Incident Factory
        incidentFactory.process(event);

        return event;
    }
}
