package com.cybersentinel.controller;

import com.cybersentinel.dto.auth.OrganizationDto;
import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.service.OrganizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationDto> getOrganizationById(@PathVariable UUID id) {
        return ApiResponse.ok(organizationService.getOrganizationById(id));
    }
}
