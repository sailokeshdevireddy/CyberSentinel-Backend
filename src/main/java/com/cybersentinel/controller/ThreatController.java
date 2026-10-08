package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.ThreatDto;
import com.cybersentinel.service.ThreatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/threats")
public class ThreatController {

    private final ThreatService threatService;

    public ThreatController(ThreatService threatService) {
        this.threatService = threatService;
    }

    @GetMapping
    public ApiResponse<List<ThreatDto>> getThreats() {
        return ApiResponse.ok(threatService.getThreats());
    }

    @GetMapping("/{id}")
    public ApiResponse<ThreatDto> getThreatById(@PathVariable UUID id) {
        return ApiResponse.ok(threatService.getThreatById(id));
    }
}
