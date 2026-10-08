package com.cybersentinel.service;

import com.cybersentinel.dto.domain.RiskAssessmentDto;
import com.cybersentinel.dto.domain.RiskFactorDto;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.RiskAssessment;
import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.model.enums.RiskSubjectType;
import com.cybersentinel.repository.RiskAssessmentRepository;
import com.cybersentinel.repository.SecurityEventRepository;
import com.cybersentinel.security.TenantContext;
import com.cybersentinel.security.TenantGuard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final SecurityEventRepository securityEventRepository;
    private final TenantContext tenantContext;
    private final TenantGuard tenantGuard;
    private final ObjectMapper objectMapper;

    public RiskAssessmentService(RiskAssessmentRepository riskAssessmentRepository,
                                 SecurityEventRepository securityEventRepository,
                                 TenantContext tenantContext,
                                 TenantGuard tenantGuard,
                                 ObjectMapper objectMapper) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.securityEventRepository = securityEventRepository;
        this.tenantContext = tenantContext;
        this.tenantGuard = tenantGuard;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public RiskAssessmentDto getRiskAssessment(RiskSubjectType subjectType, UUID subjectId) {
        // Validate tenant ownership of the subject
        if (subjectType == RiskSubjectType.INCIDENT) {
            tenantGuard.verifyIncidentAccess(subjectId);
        } else if (subjectType == RiskSubjectType.EVENT) {
            UUID orgId = tenantContext.getCurrentOrganizationId();
            securityEventRepository.findByIdAndOrganizationId(subjectId, orgId)
                    .orElseThrow(() -> ApiException.notFound("Event not found"));
        } else {
            throw ApiException.validation("Invalid subject type", null);
        }

        RiskAssessment assessment = riskAssessmentRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId)
                .orElseThrow(() -> ApiException.notFound("Risk assessment not found"));

        return mapToDto(assessment);
    }

    private RiskAssessmentDto mapToDto(RiskAssessment assessment) {
        RiskAssessmentDto dto = new RiskAssessmentDto();
        dto.setId(assessment.getId());
        dto.setSubjectType(assessment.getSubjectType());
        dto.setSubjectId(assessment.getSubjectId());
        dto.setRiskScore(assessment.getRiskScore());
        dto.setSeverity(assessment.getSeverity());
        dto.setEvaluatedAt(assessment.getEvaluatedAt());

        if (assessment.getFactors() != null) {
            try {
                List<RiskFactorDto> factors = objectMapper.readValue(
                        assessment.getFactors(),
                        new TypeReference<List<RiskFactorDto>>() {}
                );
                dto.setFactors(factors);
            } catch (JsonProcessingException e) {
                // Ignore parse errors
            }
        }
        return dto;
    }
}
