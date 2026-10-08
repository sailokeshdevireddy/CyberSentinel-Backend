package com.cybersentinel.dto.domain;

import java.util.List;
import java.util.Map;

public class DashboardResponse {
    private DashboardMetricsDto metrics;
    private Map<String, Integer> severityBreakdown;
    private List<Map<String, Object>> eventTrend;
    private List<SecurityEventDto> recentEvents;
    private List<ThreatDto> recentThreats;
    private List<IncidentDto> recentIncidents;
    private List<ApplicationDto> applicationActivity;

    public DashboardMetricsDto getMetrics() { return metrics; }
    public void setMetrics(DashboardMetricsDto metrics) { this.metrics = metrics; }
    public Map<String, Integer> getSeverityBreakdown() { return severityBreakdown; }
    public void setSeverityBreakdown(Map<String, Integer> severityBreakdown) { this.severityBreakdown = severityBreakdown; }
    public List<Map<String, Object>> getEventTrend() { return eventTrend; }
    public void setEventTrend(List<Map<String, Object>> eventTrend) { this.eventTrend = eventTrend; }
    public List<SecurityEventDto> getRecentEvents() { return recentEvents; }
    public void setRecentEvents(List<SecurityEventDto> recentEvents) { this.recentEvents = recentEvents; }
    public List<ThreatDto> getRecentThreats() { return recentThreats; }
    public void setRecentThreats(List<ThreatDto> recentThreats) { this.recentThreats = recentThreats; }
    public List<IncidentDto> getRecentIncidents() { return recentIncidents; }
    public void setRecentIncidents(List<IncidentDto> recentIncidents) { this.recentIncidents = recentIncidents; }
    public List<ApplicationDto> getApplicationActivity() { return applicationActivity; }
    public void setApplicationActivity(List<ApplicationDto> applicationActivity) { this.applicationActivity = applicationActivity; }
}
