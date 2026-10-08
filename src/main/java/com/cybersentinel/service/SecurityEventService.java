package com.cybersentinel.service;

import com.cybersentinel.dto.domain.SecurityEventDto;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.repository.SecurityEventRepository;
import com.cybersentinel.security.TenantContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;

    public SecurityEventService(SecurityEventRepository securityEventRepository,
                                TenantContext tenantContext,
                                ObjectMapper objectMapper) {
        this.securityEventRepository = securityEventRepository;
        this.tenantContext = tenantContext;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SecurityEventDto> getEvents() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        // Placeholder for real pagination/filtering logic requested by GET /api/events
        // A simple findAll for the tenant is enough for this phase unless dynamic querying is fully needed.
        return securityEventRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SecurityEventDto getEventById(UUID id) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        SecurityEvent event = securityEventRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> ApiException.notFound("Event not found"));
        return mapToDto(event);
    }

    public SecurityEventDto mapToDto(SecurityEvent event) {
        SecurityEventDto dto = new SecurityEventDto();
        dto.setId(event.getId());
        dto.setOrganizationId(event.getOrganizationId());
        dto.setApplicationId(event.getApplicationId());
        dto.setEventType(event.getEventType());
        dto.setOccurredAt(event.getOccurredAt());
        dto.setReceivedAt(event.getReceivedAt());
        dto.setUserIdentifier(event.getUserIdentifier());
        dto.setDevice(event.getDevice());
        dto.setIpAddress(event.getIpAddress());
        dto.setLocation(event.getLocation());
        dto.setRiskScore(event.getRiskScore());
        dto.setSeverity(event.getSeverity());
        dto.setStatus(event.getStatus());

        if (event.getMetadata() != null) {
            try {
                dto.setMetadata(objectMapper.readValue(event.getMetadata(), Object.class));
            } catch (JsonProcessingException e) {
                // Ignore parse errors, just leave null
            }
        }
        return dto;
    }
}
