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
    }
}
