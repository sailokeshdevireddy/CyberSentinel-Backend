package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.RiskAssessmentDto;
import com.cybersentinel.model.enums.RiskSubjectType;
import com.cybersentinel.service.RiskAssessmentService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk-assessments")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @GetMapping
    public ApiResponse<RiskAssessmentDto> getRiskAssessment(
            @RequestParam RiskSubjectType subjectType,
            @RequestParam UUID subjectId) {
        return ApiResponse.ok(riskAssessmentService.getRiskAssessment(subjectType, subjectId));
    }
}
