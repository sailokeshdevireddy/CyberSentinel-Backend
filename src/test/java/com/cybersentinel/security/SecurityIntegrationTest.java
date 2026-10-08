package com.cybersentinel.security;

import com.cybersentinel.BaseIntegrationTest;
import com.cybersentinel.dto.auth.LoginRequest;
import com.cybersentinel.dto.auth.RegisterRequest;
import com.cybersentinel.dto.auth.UpdateProfileRequest;
import com.cybersentinel.model.Organization;
import com.cybersentinel.model.User;
import com.cybersentinel.model.enums.OrganizationStatus;
import com.cybersentinel.model.enums.UserStatus;
import com.cybersentinel.repository.OrganizationRepository;
import com.cybersentinel.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    // --- REGISTER TESTS ---

    @Test
    void register_valid_returns201WithUserAndOrganization() throws Exception {
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", "alice@example.com", "password123");
        String res = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.name").value("Alice"))
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("ORG_ADMIN"))
                .andExpect(jsonPath("$.data.organization.name").value("Test Org"))
                .andExpect(jsonPath("$.data.organization.slug").value("test-org"))
                .andExpect(jsonPath("$.data.organization.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        User user = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
        assertNotEquals("password123", user.getPasswordHash());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", "alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        RegisterRequest req2 = new RegisterRequest("Another Org", "Bob", "alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
    }

    @Test
    void register_duplicateEmailDifferentCase_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", "alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        RegisterRequest req2 = new RegisterRequest("Another Org", "Bob", "ALICE@EXAMPLE.COM", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
    }

    @Test
    void register_passwordBelow8Chars_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", "alice@example.com", "pass123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_passwordAbove72Bytes_returns400() throws Exception {
        String longPass = "a".repeat(73);
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", "alice@example.com", longPass);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_slugCollision_addsSuffix() throws Exception {
        RegisterRequest req = new RegisterRequest("Example Org", "Alice", "alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.organization.slug").value("example-org"));

        RegisterRequest req2 = new RegisterRequest("Example Org", "Bob", "bob@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.organization.slug").value("example-org-2"));
    }

    // --- LOGIN TESTS ---

    private User registerUser(String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest("Test Org", "Alice", email, password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        return userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    @Test
    void login_validCredentials_returnsTokenAndUser() throws Exception {
        registerUser("alice@example.com", "password123");
        
        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        String res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("ORG_ADMIN"))
                .andReturn().getResponse().getContentAsString();
                
        String token = JsonPath.read(res, "$.data.token");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length); // valid JWT structure
        
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        assertTrue(payload.contains("userId"));
        assertTrue(payload.contains("organizationId"));
        assertTrue(payload.contains("\"role\":\"ORG_ADMIN\""));
        assertTrue(payload.contains("exp"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        registerUser("alice@example.com", "password123");
        
        LoginRequest loginReq = new LoginRequest("alice@example.com", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        LoginRequest loginReq = new LoginRequest("nobody@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_disabledUser_returns403() throws Exception {
        User user = registerUser("alice@example.com", "password123");
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    void login_suspendedOrganization_returns403() throws Exception {
        User user = registerUser("alice@example.com", "password123");
        Organization org = organizationRepository.findById(user.getOrganizationId()).orElseThrow();
        org.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(org);

        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    void login_success_updatesLastLoginAt() throws Exception {
        User user = registerUser("alice@example.com", "password123");
        assertNull(user.getLastLoginAt());

        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertNotNull(updatedUser.getLastLoginAt());
    }

    @Test
    void login_failure_doesNotUpdateLastLoginAt() throws Exception {
        User user = registerUser("alice@example.com", "password123");
        assertNull(user.getLastLoginAt());

        LoginRequest loginReq = new LoginRequest("alice@example.com", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertNull(updatedUser.getLastLoginAt());
    }

    // --- /ME TESTS ---

    private String getValidToken() throws Exception {
        registerUser("alice@example.com", "password123");
        LoginRequest loginReq = new LoginRequest("alice@example.com", "password123");
        String res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.data.token");
    }

    @Test
    void getMe_validToken_returns200() throws Exception {
        String token = getValidToken();
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    void getMe_missingToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void getMe_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer malformed-token-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_expiredToken_returns401() throws Exception {
        User user = registerUser("alice@example.com", "password123");
        
        // Generate a token that expired 1 hour ago
        Clock pastClock = Clock.fixed(Instant.now().minusSeconds(3600), ZoneId.of("UTC"));
        JwtService pastJwtService = new JwtService(pastClock, "ThisIsASecretKeyForJwtWhichMustBeAtLeast32Bytes", 1000); 
        pastJwtService.init();
        
        String expiredToken = pastJwtService.generateToken(user.getId(), user.getOrganizationId(), user.getRole().name());

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void getMe_validTokenDisabledUser_returns403() throws Exception {
        String token = getValidToken();
        User user = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    void getMe_validTokenSuspendedOrganization_returns403() throws Exception {
        String token = getValidToken();
        User user = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        Organization org = organizationRepository.findById(user.getOrganizationId()).orElseThrow();
        org.setStatus(OrganizationStatus.SUSPENDED);
        organizationRepository.save(org);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    void getMe_isNotPublic() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchMe_isNotPublic() throws Exception {
        mockMvc.perform(patch("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staleOrInvalidToken_onPublicEndpoints_doesNotCause401() throws Exception {
        mockMvc.perform(get("/api/health")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk());
    }

    // --- PATCH /ME TESTS ---

    @Test
    void patchMe_validNameUpdate_returns200() throws Exception {
        String token = getValidToken();
        UpdateProfileRequest updateReq = new UpdateProfileRequest("Alice Updated");
        
        mockMvc.perform(patch("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Alice Updated"));
    }

    @Test
    void patchMe_blankName_returns400() throws Exception {
        String token = getValidToken();
        UpdateProfileRequest updateReq = new UpdateProfileRequest("");
        
        mockMvc.perform(patch("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void patchMe_unknownField_returns400() throws Exception {
        String token = getValidToken();
        
        mockMvc.perform(patch("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{\"name\":\"Alice\",\"role\":\"HACKER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void patchMe_unauthenticated_returns401() throws Exception {
        UpdateProfileRequest updateReq = new UpdateProfileRequest("Alice Updated");
        
        mockMvc.perform(patch("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isUnauthorized());
    }

    // --- SECURITY CONFIG TESTS ---

    @Test
    void securityConfig_httpBasicAuthIsDisabled() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Basic YWxpY2U6cGFzc3dvcmQ="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securityConfig_formLoginIsDisabled() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "alice")
                .param("password", "password"))
                .andExpect(status().isUnauthorized()); // Form login endpoint doesn't exist, so it falls to anyRequest().authenticated()
    }
}
