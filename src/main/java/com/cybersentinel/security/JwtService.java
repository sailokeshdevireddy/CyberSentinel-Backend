package com.cybersentinel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Clock clock;
    private final String secret;
    private final long expirationMs;
    private SecretKey key;

    public JwtService(Clock clock,
                      @Value("${cybersentinel.jwt.secret}") String secret,
                      @Value("${cybersentinel.jwt.expiration:86400000}") long expirationMs) {
        this.clock = clock;
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UUID userId, UUID organizationId, String role) {
        long nowMillis = clock.millis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId.toString())
                .claim("organizationId", organizationId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .clock(() -> new Date(clock.millis()))
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
