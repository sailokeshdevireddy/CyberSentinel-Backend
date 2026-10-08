package com.cybersentinel.service;

import com.cybersentinel.dto.domain.ThreatDto;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Threat;
import com.cybersentinel.repository.ThreatRepository;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThreatService {

    private final ThreatRepository threatRepository;
    private final TenantContext tenantContext;

    public ThreatService(ThreatRepository threatRepository, TenantContext tenantContext) {
        this.threatRepository = threatRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public List<ThreatDto> getThreats() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        return threatRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ThreatDto getThreatById(UUID id) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        Threat threat = threatRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> ApiException.notFound("Threat not found"));
        return mapToDto(threat);
    }

    private ThreatDto mapToDto(Threat threat) {
        ThreatDto dto = new ThreatDto();
        dto.setId(threat.getId());
        dto.setOrganizationId(threat.getOrganizationId());
        dto.setApplicationId(threat.getApplicationId());
        dto.setType(threat.getType());
        dto.setSeverity(threat.getSeverity());
        dto.setRiskScore(threat.getRiskScore());
        dto.setStatus(threat.getStatus());
        if (threat.getRelatedEventIds() != null) {
            dto.setRelatedEventIds(threat.getRelatedEventIds().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList()));
        }
        dto.setCreatedAt(threat.getCreatedAt());
        dto.setUpdatedAt(threat.getUpdatedAt());
        return dto;
    }
}
