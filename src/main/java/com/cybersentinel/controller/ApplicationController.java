package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.ApplicationDto;
import com.cybersentinel.dto.domain.CreateApplicationRequest;
import com.cybersentinel.dto.domain.CreateApplicationResponse;
import com.cybersentinel.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApiResponse<List<ApplicationDto>> getApplications() {
        return ApiResponse.ok(applicationService.getApplications());
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationDto> getApplicationById(@PathVariable UUID id) {
        return ApiResponse.ok(applicationService.getApplicationById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateApplicationResponse> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return ApiResponse.ok(applicationService.createApplication(request));
    }
}
