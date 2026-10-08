package com.cybersentinel.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneId.of("UTC"));
        jwtService = new JwtService(fixedClock, "ThisIsASecretKeyForJwtWhichMustBeAtLeast32Bytes", 86400000);
        jwtService.init();
    }

    @Test
    void tokenContainsExpectedClaims() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        
        String token = jwtService.generateToken(userId, orgId, "ORG_ADMIN");
        Claims claims = jwtService.validateTokenAndGetClaims(token);
        
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(userId.toString(), claims.get("userId", String.class));
        assertEquals(orgId.toString(), claims.get("organizationId", String.class));
        assertEquals("ORG_ADMIN", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void expirationIsCorrectlyComputed() {
        String token = jwtService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "ANALYST");
        Claims claims = jwtService.validateTokenAndGetClaims(token);
        
        long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(86400000, diff);
    }

    @Test
    void validateTokenWithExpiredTokenFails() {
        Clock pastClock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneId.of("UTC"));
        JwtService pastService = new JwtService(pastClock, "ThisIsASecretKeyForJwtWhichMustBeAtLeast32Bytes", 86400000);
        pastService.init();
        
        String expiredToken = pastService.generateToken(UUID.randomUUID(), UUID.randomUUID(), "ANALYST");
        
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.validateTokenAndGetClaims(expiredToken);
        });
    }
}
