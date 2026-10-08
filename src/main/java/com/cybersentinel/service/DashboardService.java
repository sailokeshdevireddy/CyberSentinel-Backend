package com.cybersentinel.service;

import com.cybersentinel.dto.domain.DashboardMetricsDto;
import com.cybersentinel.dto.domain.DashboardResponse;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DashboardService {

    private final TenantContext tenantContext;
    private final ApplicationService applicationService;
    private final SecurityEventService securityEventService;
    private final ThreatService threatService;
    private final IncidentService incidentService;

    public DashboardService(TenantContext tenantContext,
                            ApplicationService applicationService,
                            SecurityEventService securityEventService,
                            ThreatService threatService,
                            IncidentService incidentService) {
        this.tenantContext = tenantContext;
        this.applicationService = applicationService;
        this.securityEventService = securityEventService;
        this.threatService = threatService;
        this.incidentService = incidentService;
    }

    public DashboardResponse getDashboardData() {
        // Just return a mocked structure using existing services for this phase
        // since complex aggregation requires custom queries which we want to avoid right now.
        DashboardResponse response = new DashboardResponse();
        
        DashboardMetricsDto metrics = new DashboardMetricsDto();
        metrics.setTotalApplications(applicationService.getApplications().size());
        metrics.setEventsToday(0);
        metrics.setOpenThreats(0);
        metrics.setOpenIncidents(0);
        metrics.setCriticalIncidents(0);
        metrics.setAverageRiskScore(0);
        
        response.setMetrics(metrics);
        response.setSeverityBreakdown(Collections.emptyMap());
        response.setEventTrend(Collections.emptyList());
        response.setRecentEvents(securityEventService.getEvents());
        response.setRecentThreats(threatService.getThreats());
        response.setRecentIncidents(incidentService.getIncidents());
        response.setApplicationActivity(applicationService.getApplications());

        return response;
    }
}
