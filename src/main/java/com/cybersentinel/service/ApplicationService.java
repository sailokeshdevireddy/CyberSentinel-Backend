package com.cybersentinel.service;

import com.cybersentinel.dto.domain.ApplicationDto;
import com.cybersentinel.dto.domain.CreateApplicationRequest;
import com.cybersentinel.dto.domain.CreateApplicationResponse;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Application;
import com.cybersentinel.model.enums.ApplicationStatus;
import com.cybersentinel.repository.ApplicationRepository;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TenantContext tenantContext;
    private final ApiCredentialService apiCredentialService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              TenantContext tenantContext,
                              ApiCredentialService apiCredentialService) {
        this.applicationRepository = applicationRepository;
        this.tenantContext = tenantContext;
        this.apiCredentialService = apiCredentialService;
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getApplications() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        return applicationRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplicationById(UUID id) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        Application app = applicationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> ApiException.notFound("Application not found"));
        return mapToDto(app);
    }

    @Transactional
    public CreateApplicationResponse createApplication(CreateApplicationRequest request) {
        UUID orgId = tenantContext.getCurrentOrganizationId();

        ApiCredentialService.ApiCredential creds = apiCredentialService.generateCredential();

        Application app = new Application();
        app.setOrganizationId(orgId);
        app.setName(request.getName());
        app.setDescription(request.getDescription());
        app.setStatus(ApplicationStatus.ACTIVE);
        app.setApiKey(creds.getApiKey());
        app.setSecretHash(creds.getSecretHash());
        app.setSecretMasked(creds.getSecretMasked());

        app = applicationRepository.save(app);

        ApplicationDto dto = mapToDto(app);
        return new CreateApplicationResponse(dto, creds.getRawSecret());
    }

    private ApplicationDto mapToDto(Application app) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(app.getId());
        dto.setOrganizationId(app.getOrganizationId());
        dto.setName(app.getName());
        dto.setDescription(app.getDescription());
        dto.setStatus(app.getStatus());
        dto.setApiKey(app.getApiKey());
        dto.setSecretMasked(app.getSecretMasked());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setLastEventAt(app.getLastEventAt());
        return dto;
    }
}
