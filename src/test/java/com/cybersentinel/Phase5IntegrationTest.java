package com.cybersentinel;

import com.cybersentinel.dto.domain.CreateApplicationRequest;
import com.cybersentinel.dto.domain.CreateApplicationResponse;
import com.cybersentinel.dto.domain.CreateSecurityEventRequest;
import com.cybersentinel.dto.domain.SecurityEventDto;
import com.cybersentinel.model.enums.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cybersentinel.dto.auth.RegisterRequest;
import com.cybersentinel.dto.auth.LoginRequest;
import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class Phase5IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String getValidToken() throws Exception {
        RegisterRequest regReq = new RegisterRequest("Test Org 5", "Analyst", "analyst5@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)));
        
        LoginRequest loginReq = new LoginRequest("analyst5@example.com", "password123");
        String res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.data.token");
    }

    @Test
    void testApplicationCreationAndEventIngestion() throws Exception {
        String analystToken = getValidToken();

        // Create an application
        CreateApplicationRequest appReq = new CreateApplicationRequest();
        appReq.setName("Test App 1");
        appReq.setDescription("Integration Test App");

        String appResString = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + analystToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.application.id").exists())
                .andExpect(jsonPath("$.data.application.secretMasked").exists())
                .andExpect(jsonPath("$.data.apiSecret").exists())
                .andReturn().getResponse().getContentAsString();

        CreateApplicationResponse createdApp = objectMapper.readValue(
                objectMapper.readTree(appResString).get("data").toString(),
                CreateApplicationResponse.class
        );

        String appId = createdApp.getApplication().getId().toString();
        String apiSecret = createdApp.getApiSecret();

        // Ingest a security event using the API key
        CreateSecurityEventRequest eventReq = new CreateSecurityEventRequest();
        eventReq.setEventType(EventType.LOGIN_FAILURE);
        eventReq.setOccurredAt(Instant.now());
        eventReq.setUserIdentifier("testuser");
        eventReq.setIpAddress("192.168.1.1");
        eventReq.setMetadata(Map.of("attempt", 1));

        String eventResString = mockMvc.perform(post("/api/events")
                .header("X-Application-Id", appId)
                .header("X-Application-Secret", apiSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.riskScore").value(50)) // from mocked RiskEngine
                .andExpect(jsonPath("$.data.severity").value("MEDIUM")) // from mocked RiskEngine
                .andReturn().getResponse().getContentAsString();

        SecurityEventDto eventDto = objectMapper.readValue(
                objectMapper.readTree(eventResString).get("data").toString(),
                SecurityEventDto.class
        );

        assertThat(eventDto.getEventType()).isEqualTo(EventType.LOGIN_FAILURE);
        assertThat(eventDto.getApplicationId().toString()).isEqualTo(appId);
        
        String eventId = eventDto.getId().toString();

        // Verify risk assessment was persisted
        mockMvc.perform(get("/api/risk-assessments")
                .header("Authorization", "Bearer " + analystToken)
                .param("subjectType", "EVENT")
                .param("subjectId", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskScore").value(50))
                .andExpect(jsonPath("$.data.severity").value("MEDIUM"))
                .andExpect(jsonPath("$.data.subjectId").value(eventId));
    }

    @Test
    void testBruteForceThreatDetection() throws Exception {
        String analystToken = getValidToken();

        CreateApplicationRequest appReq = new CreateApplicationRequest();
        appReq.setName("Test App Brute Force");
        appReq.setDescription("Integration Test App BF");

        String appResString = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + analystToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CreateApplicationResponse createdApp = objectMapper.readValue(
                objectMapper.readTree(appResString).get("data").toString(),
                CreateApplicationResponse.class
        );

        String appId = createdApp.getApplication().getId().toString();
        String apiSecret = createdApp.getApiSecret();

        // Ingest 5 LOGIN_FAILURE events
        for (int i = 0; i < 5; i++) {
            CreateSecurityEventRequest eventReq = new CreateSecurityEventRequest();
            eventReq.setEventType(EventType.LOGIN_FAILURE);
            eventReq.setOccurredAt(Instant.now());
            eventReq.setUserIdentifier("bruteforceuser");
            eventReq.setIpAddress("10.0.0.1");
            eventReq.setMetadata(Map.of("attempt", i));

            mockMvc.perform(post("/api/events")
                    .header("X-Application-Id", appId)
                    .header("X-Application-Secret", apiSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventReq)))
                    .andExpect(status().isCreated());
        }

        // Verify BRUTE_FORCE threat is created
        String threatsRes = mockMvc.perform(get("/api/threats")
                .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("BRUTE_FORCE"))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data[0].relatedEventIds.length()").value(5))
                .andReturn().getResponse().getContentAsString();

        String threatId = JsonPath.read(threatsRes, "$.data[0].id");

        // Verify exactly ONE incident exists and its properties
        mockMvc.perform(get("/api/incidents")
                .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.data[0].riskScore").value(80))
                .andExpect(jsonPath("$.data[0].relatedThreatIds[0]").value(threatId))
                .andExpect(jsonPath("$.data[0].relatedEventIds.length()").value(5));

        // Ingest another qualifying LOGIN_FAILURE (6th event)
        CreateSecurityEventRequest eventReq6 = new CreateSecurityEventRequest();
        eventReq6.setEventType(EventType.LOGIN_FAILURE);
        eventReq6.setOccurredAt(Instant.now());
        eventReq6.setUserIdentifier("bruteforceuser");
        eventReq6.setIpAddress("10.0.0.1");
        eventReq6.setMetadata(Map.of("attempt", 5));

        mockMvc.perform(post("/api/events")
                .header("X-Application-Id", appId)
                .header("X-Application-Secret", apiSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventReq6)))
                .andExpect(status().isCreated());

        // Verify no duplicate incident is created
        mockMvc.perform(get("/api/incidents")
                .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].relatedEventIds.length()").value(5)); // The incident's relatedEventIds might not be updated automatically unless we specifically code it to append. The requirement just says "no duplicate incident is created", length 1 is the main check.
    }
}
