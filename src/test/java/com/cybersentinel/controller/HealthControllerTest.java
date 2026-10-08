package com.cybersentinel.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cybersentinel.dto.common.HealthResponse;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.service.HealthService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void healthReturnsSuccessEnvelope() throws Exception {
        when(healthService.check()).thenReturn(new HealthResponse(
                "UP", "cybersentinel", "cybersentinel", Instant.parse("2026-10-01T10:00:00Z")));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").value("cybersentinel"))
                .andExpect(jsonPath("$.data.timestamp").value("2026-10-01T10:00:00Z"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.meta").doesNotExist());
    }

    @Test
    void databaseDownReturnsServiceUnavailableEnvelope() throws Exception {
        when(healthService.check()).thenThrow(ApiException.serviceUnavailable("Database is unreachable"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.error.message").value("Database is unreachable"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"details\":null")))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void unknownRouteReturnsNotFoundEnvelope() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void wrongMethodReturnsMethodNotAllowedEnvelope() throws Exception {
        mockMvc.perform(post("/api/health"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
    }
}
