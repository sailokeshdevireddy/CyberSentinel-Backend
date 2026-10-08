package com.cybersentinel.service;

import com.cybersentinel.model.SecurityEvent;
import com.cybersentinel.model.enums.Severity;
import org.springframework.stereotype.Service;

@Service
public class RiskEngine {
    
    public void evaluateEvent(SecurityEvent event) {
        // Simple mock evaluation to fulfill architecture requirement
        event.setRiskScore(50);
        event.setSeverity(Severity.MEDIUM);
    }
}
