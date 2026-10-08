package com.cybersentinel.security;

import com.cybersentinel.BaseIntegrationTest;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.*;
import com.cybersentinel.model.enums.*;
import com.cybersentinel.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.cybersentinel.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantIsolationIntegrationTest extends BaseIntegrationTest {

    @Autowired private OrganizationRepository orgRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private IncidentRepository incidentRepo;
    @Autowired private IncidentNoteRepository noteRepo;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private TenantContext tenantContext;
    @Autowired private TenantGuard tenantGuard;

    private Organization orgA;
    private Organization orgB;
    private User userA;
    private User userB;
    private User userC;
    private Application appA;
    private Application appB;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        notificationRepo.deleteAllInBatch();
        noteRepo.deleteAllInBatch();
        incidentRepo.deleteAllInBatch();
        appRepo.deleteAllInBatch();
        userRepo.deleteAllInBatch();
        orgRepo.deleteAllInBatch();
        
        orgA = new Organization();
        orgA.setName("Org A");
        orgA.setSlug("org-a");
        orgA.setStatus(OrganizationStatus.ACTIVE);
        orgA = orgRepo.saveAndFlush(orgA);

        orgB = new Organization();
        orgB.setName("Org B");
        orgB.setSlug("org-b");
        orgB.setStatus(OrganizationStatus.ACTIVE);
        orgB = orgRepo.saveAndFlush(orgB);

        userA = new User();
        userA.setOrganizationId(orgA.getId());
        userA.setName("User A");
        userA.setEmail("usera@example.com");
        userA.setPasswordHash("hash");
        userA.setRole(UserRole.ANALYST);
        userA.setStatus(UserStatus.ACTIVE);
        userA = userRepo.saveAndFlush(userA);

        userB = new User();
        userB.setOrganizationId(orgB.getId());
        userB.setName("User B");
        userB.setEmail("userb@example.com");
        userB.setPasswordHash("hash");
        userB.setRole(UserRole.ANALYST);
        userB.setStatus(UserStatus.ACTIVE);
        userB = userRepo.saveAndFlush(userB);

        userC = new User();
        userC.setOrganizationId(orgA.getId());
        userC.setName("User C");
        userC.setEmail("userc@example.com");
        userC.setPasswordHash("hash");
        userC.setRole(UserRole.ANALYST);
        userC.setStatus(UserStatus.ACTIVE);
        userC = userRepo.saveAndFlush(userC);

        appA = new Application();
        appA.setOrganizationId(orgA.getId());
        appA.setName("App A");
        appA.setApiKey("api-a");
        appA.setSecretMasked("mask");
        appA.setSecretHash("hash");
        appA.setStatus(ApplicationStatus.ACTIVE);
        appA = appRepo.saveAndFlush(appA);

        appB = new Application();
        appB.setOrganizationId(orgB.getId());
        appB.setName("App B");
        appB.setApiKey("api-b");
        appB.setSecretMasked("mask");
        appB.setSecretHash("hash");
        appB.setStatus(ApplicationStatus.ACTIVE);
        appB = appRepo.saveAndFlush(appB);
    }

    private void mockSecurityContext(User user) {
        AuthenticatedUser authUser = new AuthenticatedUser(user.getId(), user.getOrganizationId(), user.getRole());
        SecurityContextHolder.getContext().setAuthentication(authUser);
    }

    @Test
    void testTenantContextFailsClosed() {
        // No security context set
        assertThrows(IllegalStateException.class, () -> tenantContext.getCurrentOrganizationId());
    }

    @Test
    void testSameTenantAccessSucceeds() {
        mockSecurityContext(userA);
        
        Incident incident = new Incident();
        incident.setOrganizationId(orgA.getId());
        incident.setApplicationId(appA.getId());
        incident.setTitle("Org A Incident");
        incident.setSeverity(Severity.HIGH);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setRiskScore(50);
        incident = incidentRepo.saveAndFlush(incident);

        // Same tenant can access
        assertThat(tenantContext.getCurrentOrganizationId()).isEqualTo(orgA.getId());
        
        // TenantGuard succeeds silently for same org
        tenantGuard.verifyIncidentAccess(incident.getId());
        
        // Direct repository call returns value
        assertThat(incidentRepo.findByIdAndOrganizationId(incident.getId(), orgA.getId())).isPresent();
    }

    @Test
    void testCrossTenantAccessReturns404() {
        mockSecurityContext(userA); // User from Org A
        
        Incident incidentB = new Incident();
        incidentB.setOrganizationId(orgB.getId());
        incidentB.setApplicationId(appB.getId());
        incidentB.setTitle("Org B Incident");
        incidentB.setSeverity(Severity.HIGH);
        incidentB.setStatus(IncidentStatus.OPEN);
        incidentB.setRiskScore(50);
        Incident savedIncidentB = incidentRepo.saveAndFlush(incidentB);

        // Direct repository access returns empty
        assertThat(incidentRepo.findByIdAndOrganizationId(savedIncidentB.getId(), orgA.getId())).isEmpty();

        // TenantGuard throws NOT_FOUND
        ApiException ex = assertThrows(ApiException.class, () -> tenantGuard.verifyIncidentAccess(savedIncidentB.getId()));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void testListQueriesReturnOnlyCurrentOrganizationData() {
        mockSecurityContext(userA);
        
        Incident incidentA = new Incident();
        incidentA.setOrganizationId(orgA.getId());
        incidentA.setApplicationId(appA.getId());
        incidentA.setTitle("Org A Incident");
        incidentA.setSeverity(Severity.HIGH);
        incidentA.setStatus(IncidentStatus.OPEN);
        incidentA.setRiskScore(50);
        incidentRepo.saveAndFlush(incidentA);

        Incident incidentB = new Incident();
        incidentB.setOrganizationId(orgB.getId());
        incidentB.setApplicationId(appB.getId());
        incidentB.setTitle("Org B Incident");
        incidentB.setSeverity(Severity.HIGH);
        incidentB.setStatus(IncidentStatus.OPEN);
        incidentB.setRiskScore(50);
        incidentRepo.saveAndFlush(incidentB);

        List<Incident> results = incidentRepo.findByOrganizationId(orgA.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Org A Incident");
    }

    @Test
    void testChildEntitiesCannotBeAccessedThroughAnotherOrganizationParent() {
        mockSecurityContext(userA);
        
        Incident incidentB = new Incident();
        incidentB.setOrganizationId(orgB.getId());
        incidentB.setApplicationId(appB.getId());
        incidentB.setTitle("Org B Incident");
        incidentB.setSeverity(Severity.HIGH);
        incidentB.setStatus(IncidentStatus.OPEN);
        incidentB.setRiskScore(50);
        Incident savedIncidentB = incidentRepo.saveAndFlush(incidentB);

        IncidentNote note = new IncidentNote();
        note.setIncidentId(savedIncidentB.getId());
        note.setAuthorId(userB.getId());
        note.setAuthorName(userB.getName());
        note.setNote("Secret note");
        note = noteRepo.saveAndFlush(note);

        // User A tries to get the note by first verifying the incident
        // TenantGuard should throw 404 because incidentB belongs to Org B
        ApiException ex = assertThrows(ApiException.class, () -> tenantGuard.verifyIncidentAccess(savedIncidentB.getId()));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void testNotificationOrganizationAndUserBroadcastScoping() {
        Notification broadcastOrgA = new Notification();
        broadcastOrgA.setOrganizationId(orgA.getId());
        broadcastOrgA.setUserId(null); // Broadcast
        broadcastOrgA.setType(NotificationType.SYSTEM);
        broadcastOrgA.setTitle("Broadcast A");
        broadcastOrgA.setMessage("msg");
        broadcastOrgA.setRelatedEntityType("SYSTEM");
        notificationRepo.saveAndFlush(broadcastOrgA);

        Notification specificUserA = new Notification();
        specificUserA.setOrganizationId(orgA.getId());
        specificUserA.setUserId(userA.getId()); // Specific to A
        specificUserA.setType(NotificationType.SYSTEM);
        specificUserA.setTitle("Specific A");
        specificUserA.setMessage("msg");
        specificUserA.setRelatedEntityType("SYSTEM");
        notificationRepo.saveAndFlush(specificUserA);

        Notification specificOtherUserOrgA = new Notification();
        specificOtherUserOrgA.setOrganizationId(orgA.getId());
        specificOtherUserOrgA.setUserId(userC.getId()); // Specific to some other user in A
        specificOtherUserOrgA.setType(NotificationType.SYSTEM);
        specificOtherUserOrgA.setTitle("Specific Other");
        specificOtherUserOrgA.setMessage("msg");
        specificOtherUserOrgA.setRelatedEntityType("SYSTEM");
        notificationRepo.saveAndFlush(specificOtherUserOrgA);

        Notification broadcastOrgB = new Notification();
        broadcastOrgB.setOrganizationId(orgB.getId());
        broadcastOrgB.setUserId(null); // Broadcast
        broadcastOrgB.setType(NotificationType.SYSTEM);
        broadcastOrgB.setTitle("Broadcast B");
        broadcastOrgB.setMessage("msg");
        broadcastOrgB.setRelatedEntityType("SYSTEM");
        notificationRepo.saveAndFlush(broadcastOrgB);

        mockSecurityContext(userA);
        List<Notification> results = notificationRepo.findUserNotifications(orgA.getId(), userA.getId());
        
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Notification::getTitle).containsExactlyInAnyOrder("Broadcast A", "Specific A");
    }
}
