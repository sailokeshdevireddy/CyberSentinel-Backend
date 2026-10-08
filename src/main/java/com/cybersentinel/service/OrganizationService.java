package com.cybersentinel.service;

import com.cybersentinel.dto.auth.OrganizationDto;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Organization;
import com.cybersentinel.repository.OrganizationRepository;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final TenantContext tenantContext;

    public OrganizationService(OrganizationRepository organizationRepository, TenantContext tenantContext) {
        this.organizationRepository = organizationRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationById(UUID id) {
        UUID currentOrgId = tenantContext.getCurrentOrganizationId();
        if (!currentOrgId.equals(id)) {
            throw ApiException.notFound("Organization not found");
        }
        
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Organization not found"));
        
        return new OrganizationDto(org.getId(), org.getName(), org.getSlug(), org.getStatus());
    }
}
