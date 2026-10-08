package com.cybersentinel.dto.auth;

import java.time.Instant;

public record LoginResponse(
    String token,
    Instant expiresAt,
    AuthUserDto user
) {}
