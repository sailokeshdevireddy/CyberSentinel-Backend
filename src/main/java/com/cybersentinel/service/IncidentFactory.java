package com.cybersentinel.service;

import com.cybersentinel.model.Incident;
import com.cybersentinel.model.Threat;
import com.cybersentinel.model.enums.IncidentStatus;
import com.cybersentinel.model.enums.Severity;
import com.cybersentinel.repository.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentFactory {
    
    private final IncidentRepository incidentRepository;

    public IncidentFactory(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public void process(Threat threat) {
        if (threat.getRiskScore() >= 70 && (threat.getSeverity() == Severity.HIGH || threat.getSeverity() == Severity.CRITICAL)) {
            List<Incident> openIncidents = incidentRepository.findByStatusAndRelatedThreatId(IncidentStatus.OPEN, threat.getId());
            if (openIncidents.isEmpty()) {
                Incident incident = new Incident();
                incident.setOrganizationId(threat.getOrganizationId());
                incident.setApplicationId(threat.getApplicationId());
                incident.setTitle(threat.getType().name() + " Detected");
                incident.setSeverity(threat.getSeverity());
                incident.setRiskScore(threat.getRiskScore());
                incident.setStatus(IncidentStatus.OPEN);
                
                List<java.util.UUID> threatIds = new ArrayList<>();
                threatIds.add(threat.getId());
                incident.setRelatedThreatIds(threatIds);
                
                if (threat.getRelatedEventIds() != null) {
                    incident.setRelatedEventIds(new ArrayList<>(threat.getRelatedEventIds()));
                } else {
                    incident.setRelatedEventIds(new ArrayList<>());
                }
                
                incidentRepository.save(incident);
            }
        }
    }
}
