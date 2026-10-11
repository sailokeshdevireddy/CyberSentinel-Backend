package com.cybersentinel.service;

import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.model.Threat;
import com.cybersentinel.model.enums.EventType;
import com.cybersentinel.model.enums.Severity;
import com.cybersentinel.model.enums.ThreatStatus;
import com.cybersentinel.model.enums.ThreatType;
import com.cybersentinel.repository.SecurityEventRepository;
import com.cybersentinel.repository.ThreatRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThreatDetector {
    
    private final SecurityEventRepository securityEventRepository;
    private final ThreatRepository threatRepository;

    public ThreatDetector(SecurityEventRepository securityEventRepository, ThreatRepository threatRepository) {
        this.securityEventRepository = securityEventRepository;
        this.threatRepository = threatRepository;
    }

    public List<Threat> analyze(SecurityEvent event) {
        List<Threat> detectedThreats = new ArrayList<>();
        if (event.getEventType() == EventType.LOGIN_FAILURE) {
            Instant tenMinsAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
            List<SecurityEvent> recentFailures = securityEventRepository.findByEventTypeAndUserIdentifierAndIpAddressAndApplicationIdAndOccurredAtGreaterThanEqual(
                    EventType.LOGIN_FAILURE,
                    event.getUserIdentifier(),
                    event.getIpAddress(),
                    event.getApplicationId(),
                    tenMinsAgo
            );

            if (recentFailures.size() >= 5) {
                List<UUID> eventIds = recentFailures.stream().map(SecurityEvent::getId).collect(Collectors.toList());
                List<Threat> openThreats = threatRepository.findRelatedOpenThreats(
                        ThreatStatus.OPEN,
                        ThreatType.BRUTE_FORCE,
                        event.getApplicationId(),
                        eventIds
                );

                Threat threat;
                if (!openThreats.isEmpty()) {
                    threat = openThreats.get(0);
                    // Append only missing events to avoid duplicates
                    List<UUID> existingIds = threat.getRelatedEventIds();
                    for (UUID id : eventIds) {
                        if (!existingIds.contains(id)) {
                            existingIds.add(id);
                        }
                    }
                } else {
                    threat = new Threat();
                    threat.setOrganizationId(event.getOrganizationId());
                    threat.setApplicationId(event.getApplicationId());
                    threat.setType(ThreatType.BRUTE_FORCE);
                    threat.setSeverity(Severity.HIGH);
                    threat.setRiskScore(80);
                    threat.setStatus(ThreatStatus.OPEN);
                    threat.setRelatedEventIds(new ArrayList<>(eventIds));
                }
                threat = threatRepository.save(threat);
                detectedThreats.add(threat);
            }
        }
        return detectedThreats;
    }
}
