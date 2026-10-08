package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.AuditLogDto;
import com.cybersentinel.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogDto>> getAuditLogs() {
        return ApiResponse.ok(auditLogService.getAuditLogs());
    }
}
