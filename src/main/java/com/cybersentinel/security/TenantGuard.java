package com.cybersentinel.security;

import com.cybersentinel.exception.ApiException;
import com.cybersentinel.repository.IncidentRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TenantGuard {

    private final TenantContext tenantContext;
    private final IncidentRepository incidentRepository;

    public TenantGuard(TenantContext tenantContext, IncidentRepository incidentRepository) {
        this.tenantContext = tenantContext;
        this.incidentRepository = incidentRepository;
    }

    public void verifyIncidentAccess(UUID incidentId) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        incidentRepository.findByIdAndOrganizationId(incidentId, orgId)
                .orElseThrow(() -> ApiException.notFound("Incident not found"));
    }
}
