package com.cybersentinel.service;

import com.cybersentinel.dto.domain.AuditLogDto;
import com.cybersentinel.model.AuditLog;
import com.cybersentinel.repository.AuditLogRepository;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final TenantContext tenantContext;

    public AuditLogService(AuditLogRepository auditLogRepository, TenantContext tenantContext) {
        this.auditLogRepository = auditLogRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogs() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        return auditLogRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuditLogDto mapToDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setOrganizationId(log.getOrganizationId());
        dto.setActorId(log.getActorId());
        dto.setActorName(log.getActorName());
        dto.setAction(log.getAction());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setResult(log.getResult() != null ? log.getResult().name() : null);
        dto.setIpAddress(log.getIpAddress());
        dto.setOccurredAt(log.getOccurredAt());
        return dto;
    }
}
