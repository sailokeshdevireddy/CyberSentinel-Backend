package com.cybersentinel.security;

import com.cybersentinel.dto.common.ApiError;
import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.model.Organization;
import com.cybersentinel.model.User;
import com.cybersentinel.model.enums.OrganizationStatus;
import com.cybersentinel.model.enums.UserStatus;
import com.cybersentinel.repository.OrganizationRepository;
import com.cybersentinel.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   OrganizationRepository organizationRepository,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // All OPTIONS preflight requests must bypass JWT so the CORS filter
        // can respond before any authentication check runs.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return (path.equals("/api/auth/register") && method.equals("POST")) ||
               (path.equals("/api/auth/login") && method.equals("POST")) ||
               (path.equals("/api/health") && method.equals("GET"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.validateTokenAndGetClaims(token);
            String userIdStr = claims.get("userId", String.class);

            if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = UUID.fromString(userIdStr);

                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Organization organization = organizationRepository.findById(user.getOrganizationId()).orElse(null);
                if (organization == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if (user.getStatus() == UserStatus.DISABLED || organization.getStatus() == OrganizationStatus.SUSPENDED) {
                    writeAccountDisabledResponse(response);
                    return;
                }

                AuthenticatedUser authUser = new AuthenticatedUser(user.getId(), organization.getId(), user.getRole());
                SecurityContextHolder.getContext().setAuthentication(authUser);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void writeAccountDisabledResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = new ApiError("ACCOUNT_DISABLED", "User or Organization is disabled", null);
        ApiResponse<Void> apiResponse = ApiResponse.failure(error);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
