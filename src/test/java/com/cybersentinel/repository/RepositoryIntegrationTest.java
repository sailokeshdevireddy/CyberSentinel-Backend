package com.cybersentinel.repository;

import com.cybersentinel.BaseIntegrationTest;
import com.cybersentinel.model.*;
import com.cybersentinel.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired private OrganizationRepository orgRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private SecurityEventRepository eventRepo;
    @Autowired private RiskAssessmentRepository riskRepo;
    @Autowired private ThreatRepository threatRepo;
    @Autowired private IncidentRepository incidentRepo;
    @Autowired private IncidentNoteRepository noteRepo;
    @Autowired private IncidentTimelineEntryRepository timelineRepo;
    @Autowired private AuditLogRepository auditRepo;
    @Autowired private NotificationRepository notificationRepo;

    @Test
    @Transactional
    void canSaveAndReadEntities() {
        Organization org = new Organization();
        org.setName("Test Org");
        org.setSlug("test-org-1");
        org.setStatus(OrganizationStatus.ACTIVE);
        org = orgRepo.saveAndFlush(org);

        User user = new User();
        user.setOrganizationId(org.getId());
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("dummyhash");
        user.setRole(UserRole.ANALYST);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepo.saveAndFlush(user);

        Application app = new Application();
        app.setOrganizationId(org.getId());
        app.setName("Test App");
        app.setApiKey("test-api-key");
        app.setSecretMasked("masked");
        app.setSecretHash("hash");
        app.setStatus(ApplicationStatus.ACTIVE);
        app = appRepo.saveAndFlush(app);

        SecurityEvent event = new SecurityEvent();
        event.setOrganizationId(org.getId());
        event.setApplicationId(app.getId());
        event.setEventType(EventType.LOGIN_FAILURE);
        event.setOccurredAt(Instant.now());
        event.setReceivedAt(Instant.now());
        event.setStatus(EventStatus.RECEIVED);
        event = eventRepo.saveAndFlush(event);

        Threat threat = new Threat();
        threat.setOrganizationId(org.getId());
        threat.setApplicationId(app.getId());
        threat.setType(ThreatType.BRUTE_FORCE);
        threat.setSeverity(Severity.HIGH);
        threat.setRiskScore(80);
        threat.setStatus(ThreatStatus.OPEN);
        threat.setRelatedEventIds(List.of(event.getId()));
        threat = threatRepo.saveAndFlush(threat);

        Incident incident = new Incident();
        incident.setOrganizationId(org.getId());
        incident.setApplicationId(app.getId());
        incident.setTitle("Test Incident");
        incident.setSeverity(Severity.HIGH);
        incident.setRiskScore(85);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setRelatedThreatIds(List.of(threat.getId()));
        incident.setRelatedEventIds(List.of(event.getId()));
        incident = incidentRepo.saveAndFlush(incident);

        assertThat(incident.getId()).isNotNull();
        assertThat(threatRepo.findById(threat.getId()).get().getRelatedEventIds()).hasSize(1);
        assertThat(incidentRepo.findById(incident.getId()).get().getRelatedThreatIds()).hasSize(1);
    }

    @Test
    void uniqueConstraintViolations() {
        Organization org = new Organization();
        org.setName("Org 1");
        org.setSlug("duplicate-slug");
        org.setStatus(OrganizationStatus.ACTIVE);
        orgRepo.saveAndFlush(org);

        Organization org2 = new Organization();
        org2.setName("Org 2");
        org2.setSlug("duplicate-slug");
        org2.setStatus(OrganizationStatus.ACTIVE);

        assertThrows(DataIntegrityViolationException.class, () -> {
            orgRepo.saveAndFlush(org2);
        });
    }
}
