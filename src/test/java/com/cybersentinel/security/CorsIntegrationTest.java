package com.cybersentinel.security;

import com.cybersentinel.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that Spring Security's CORS/preflight handling works correctly for
 * the production Vercel frontend origin.
 *
 * The property override below sets CORS_ALLOWED_ORIGINS to the Vercel URL so
 * the test exercises the same code path as production, without requiring the
 * environment variable to be present in CI.
 */
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "cybersentinel.cors.allowed-origins=https://cyber-sentinel-o0oh0xriz-reddy6.vercel.app"
})
public class CorsIntegrationTest extends BaseIntegrationTest {

    private static final String PROD_ORIGIN = "https://cyber-sentinel-o0oh0xriz-reddy6.vercel.app";
    private static final String UNKNOWN_ORIGIN = "https://evil.example.com";

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // Preflight (OPTIONS) tests
    // -----------------------------------------------------------------------

    @Test
    void options_preflightFromAllowedOrigin_returns200WithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, PROD_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PROD_ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
    }

    @Test
    void options_preflightFromRejectedOrigin_returnsNoCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void options_preflightForAuthenticatedEndpoint_doesNotRequireToken() throws Exception {
        // Preflight for a secured endpoint must succeed without a JWT token.
        // Previously the JWT filter intercepted OPTIONS and could return 401.
        mockMvc.perform(options("/api/auth/me")
                .header(HttpHeaders.ORIGIN, PROD_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PROD_ORIGIN));
    }

    // -----------------------------------------------------------------------
    // Real cross-origin request tests
    // -----------------------------------------------------------------------

    @Test
    void crossOriginRequest_fromAllowedOrigin_includesAllowOriginHeader() throws Exception {
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, PROD_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PROD_ORIGIN));
    }

    @Test
    void crossOriginRequest_fromRejectedOrigin_hasNoCorsHeader() throws Exception {
        // The request may succeed (public endpoint) but must not carry CORS headers.
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, UNKNOWN_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void crossOriginPost_loginFromAllowedOrigin_includesAllowOriginHeader() throws Exception {
        // Even a 401 response must carry the CORS header so the browser can read the error body.
        mockMvc.perform(post("/api/auth/login")
                .header(HttpHeaders.ORIGIN, PROD_ORIGIN)
                .contentType("application/json")
                .content("{\"email\":\"nobody@example.com\",\"password\":\"wrong\"}"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PROD_ORIGIN));
    }
}
