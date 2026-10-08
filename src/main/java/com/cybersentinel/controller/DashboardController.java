package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.DashboardResponse;
import com.cybersentinel.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.ok(dashboardService.getDashboardData());
    }
}
