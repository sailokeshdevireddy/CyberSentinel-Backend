package com.cybersentinel.dto.domain;

public class DashboardMetricsDto {
    private int totalApplications;
    private int eventsToday;
    private int openThreats;
    private int openIncidents;
    private int criticalIncidents;
    private int averageRiskScore;

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
    public int getEventsToday() { return eventsToday; }
    public void setEventsToday(int eventsToday) { this.eventsToday = eventsToday; }
    public int getOpenThreats() { return openThreats; }
    public void setOpenThreats(int openThreats) { this.openThreats = openThreats; }
    public int getOpenIncidents() { return openIncidents; }
    public void setOpenIncidents(int openIncidents) { this.openIncidents = openIncidents; }
    public int getCriticalIncidents() { return criticalIncidents; }
    public void setCriticalIncidents(int criticalIncidents) { this.criticalIncidents = criticalIncidents; }
    public int getAverageRiskScore() { return averageRiskScore; }
    public void setAverageRiskScore(int averageRiskScore) { this.averageRiskScore = averageRiskScore; }
}
